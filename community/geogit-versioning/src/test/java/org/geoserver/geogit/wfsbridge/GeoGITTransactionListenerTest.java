package org.geoserver.geogit.wfsbridge;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import net.opengis.wfs.TransactionType;

import org.geoserver.geogit.GEOGIT;
import org.geoserver.wfs.TransactionEvent;

public class GeoGITTransactionListenerTest extends TestCase {

    private GeoGITTransactionListener listener;

    private GEOGIT mockGssFacade;

    protected void setUp() throws Exception {
        mockGssFacade = mock(GEOGIT.class);
        listener = new GeoGITTransactionListener(mockGssFacade);
    }

    public void testBeforeTransaction() {
        Map<Object, Object> extendedProperties = new HashMap<Object, Object>();
        TransactionType request = mock(TransactionType.class);
        when(request.getExtendedProperties()).thenReturn(extendedProperties);
        listener.beforeTransaction(request);
        assertTrue(extendedProperties.get(GeoGITTransactionListener.GEOGIT_TRANSACTION_UUID) instanceof String);
    }

    public void testDataStoreChange() {
        TransactionEvent event = mock(TransactionEvent.class);
        listener.dataStoreChange(event);
        fail("Not yet implemented");
    }

    public void testBeforeCommit() {
        fail("Not yet implemented");
    }

    public void testAfterTransaction() {
        fail("Not yet implemented");
    }

}
