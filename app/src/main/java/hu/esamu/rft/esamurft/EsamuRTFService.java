package hu.esamu.rft.esamurft;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class EsamuRTFService extends Service {
    public EsamuRTFService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
