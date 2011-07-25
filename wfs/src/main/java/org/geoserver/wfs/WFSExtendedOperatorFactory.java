/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.platform.FileWatcher;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.template.FeatureWrapper;
import org.geotools.feature.NameImpl;
import org.geotools.filter.ExtendedOperatorFactory;
import org.geotools.filter.FunctionFactory;
import org.geotools.filter.FunctionImpl;
import org.geotools.filter.capability.FunctionNameImpl;
import org.geotools.util.Converters;
import org.geotools.util.logging.Logging;
import org.opengis.feature.Feature;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.capability.FunctionName;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import freemarker.template.Configuration;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class WFSExtendedOperatorFactory implements ExtendedOperatorFactory, FunctionFactory {

    static Logger LOGGER = Logging.getLogger(WFSExtendedOperatorFactory.class);

    /**
     * The template configuration used for operator templates
     */
    static Configuration templateConfig;
    static {
        templateConfig = new Configuration();
    }

    static FeatureWrapper featureWrapper = new FeatureWrapper();

    volatile GeoServerDataDirectory dataDir;
    volatile File opRoot;
    volatile OpIndexFileWatcher opIndexWatcher;
    volatile Map<Name,Operator> opIndex;

    @Override
    public List<Name> getOperatorNames() {
        try {
            Map<Name,Operator> opIndex = opIndex();
            if (opIndex != null) {
                return new ArrayList(opIndex.keySet());
            }
        }
        catch(Exception e) {
            LOGGER.warning("Exception occured during extended operator name lookup, set logging to " +
                "FINE to view exception");
            LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public Filter operator(Name name, List<Expression> args, FilterFactory factory) {
        try {
            Map<Name,Operator> opIndex = opIndex();
            if (opIndex != null && opIndex.containsKey(name)) {
                Operator op = opIndex.get(name);
                return factory.equal(new ExtendedOperator(op, args), factory.literal(true), true);
            }
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<FunctionName> getFunctionNames() {
        try {
            Map<Name,Operator> opIndex = opIndex();
            if (opIndex != null) {
                List<FunctionName> names = new ArrayList();
                for (Name n : opIndex.keySet()) {
                    names.add(toFunctionName(n));
                }
                return names;
            }
        }
        catch(Exception e) {
            LOGGER.warning("Exception occured during extended operator name lookup, set logging to " +
                "FINE to view exception");
            LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public Function function(String functionName, List<Expression> args, Literal fallback) {
        Name name = fromFunctionName(functionName);
        if (name != null) {
            try {
                Map<Name,Operator> opIndex = opIndex();
                if (opIndex != null && opIndex.containsKey(name)) {
                    Operator op = opIndex.get(name);
                    return new ExtendedOperator(op, args);
                }
            }
            catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    static String string(Name name) {
        return name.getNamespaceURI() + "#" + name.getLocalPart();
    }

    FunctionName toFunctionName(Name name) {
        return new FunctionNameImpl(string(name));
    }

    Name fromFunctionName(String name) {
        if (name.contains("#")) {
            String[] split = name.split("#");
            return new NameImpl(split[0], split[1]);
        }
        return null;
    }

    Map<Name, Operator> opIndex() {
        try {
            OpIndexFileWatcher opIndexWatcher = opIndexWatcher();
            if (opIndexWatcher.isModified()) {
                synchronized (this) {
                    if (opIndexWatcher.isModified()) {
                        opIndex = opIndexWatcher.read();
                    }
                }
            }
            return opIndex;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    OpIndexFileWatcher opIndexWatcher() throws IOException {
        if (opIndexWatcher == null) {
            synchronized (this) {
                if (opIndexWatcher == null) {
                    opIndexWatcher = new OpIndexFileWatcher(new File(opRoot(), "ops.xml"));
                }
            }
        }
        return opIndexWatcher;
    }

    GeoServerDataDirectory dataDir() {
        if (dataDir == null) {
            synchronized (this) {
                if (dataDir == null) {
                    dataDir = GeoServerExtensions.bean(GeoServerDataDirectory.class);
                }
            }
        }
        return dataDir;
    }

    File opRoot() throws IOException {
        if (opRoot == null) {
            synchronized (this) {
                if (opRoot == null) {
                    opRoot = dataDir().findOrCreateDir("wfs", "operators");
                    templateConfig.setDirectoryForTemplateLoading(opRoot);
                }
            }
        }
        return opRoot;
    }

    static class ExtendedOperator extends FunctionImpl {
        Operator op;
        
        ExtendedOperator(Operator op, List<Expression> expressions) {
            this.op = op;
            setName(string(op.name));
            setParameters(expressions);
        }

        @Override
        public Object evaluate(Object object) {
            try {
                Template t = null;
                synchronized (templateConfig) {
                    t = templateConfig.getTemplate(op.template.getName());
                }
                
                //create the template model
                SimpleHash model = new SimpleHash();
                
                //use the feature wrapper to freate the feature model
                model.put("feature",featureWrapper.wrap(object));

                //TODO: we probably want to do the string conversion here like the other templates do

                //create the args argument
                List args = new ArrayList();
                for (Expression p : getParameters()) {
                    Object val = p.evaluate(object); 
                    args.add(val != null ? val : "");
                }
                
                model.put("args", args);
                
                StringWriter w = new StringWriter();
                t.process(model, w);
                
                Boolean result = Converters.convert(w.toString().trim(), Boolean.class); 
                result = result != null ? result : false;
                return result;
            } 
            catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class Operator {
        Name name;
        File schema;
        File template;

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((schema == null) ? 0 : schema.hashCode());
            result = prime * result + ((template == null) ? 0 : template.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Operator other = (Operator) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            if (schema == null) {
                if (other.schema != null)
                    return false;
            } else if (!schema.equals(other.schema))
                return false;
            if (template == null) {
                if (other.template != null)
                    return false;
            } else if (!template.equals(other.template))
                return false;
            return true;
        }
    }

    class OpIndexFileWatcher extends FileWatcher<Map<Name,Operator>> {

        DocumentBuilder db;

        public OpIndexFileWatcher(File file) throws IOException {
            super(file);

            try {
                db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                throw new IOException(e);
            }
        }

        @Override
        protected Map<Name, Operator> parseFileContents(InputStream in) throws IOException {
            //structure of xml file:
            //<ops>
            // <op name="" namespace="" schema="" template=""/>
            //</ops>
            Map<Name,Operator> index = new LinkedHashMap();
            Document doc;
            try {
                doc = db.parse(in);
            } catch (SAXException e) {
                throw new IOException(e);
            }
            NodeList opsList = doc.getElementsByTagName("op");
            for (int i = 0; i < opsList.getLength(); i++) {
                Element opElement = (Element) opsList.item(i);

                Operator op = new Operator();
                op.name = 
                    new NameImpl(opElement.getAttribute("namespace"),opElement.getAttribute("name"));
                op.schema = new File(opRoot(), opElement.getAttribute("schema"));
                op.template = new File(opRoot(), opElement.getAttribute("template"));

                index.put(op.name, op);
            }
        
            return index;
        }
    }
}
