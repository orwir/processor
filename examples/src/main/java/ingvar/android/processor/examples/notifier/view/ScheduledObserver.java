package ingvar.android.processor.examples.notifier.view;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import ingvar.android.processor.examples.R;
import ingvar.android.processor.observation.ContextObserver;

/**
 * Created by Igor Zubenko on 2015.07.09.
 */
public class ScheduledObserver extends ContextObserver<Context, Void> {

    protected static int id = 0;

    public ScheduledObserver(Context context) {
        super(context);
    }

    @Override
    public void completed(Void result) {
        Context context = getContext();

        Notification notification = new NotificationCompat.Builder(context)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getText(R.string.label_dummy_notification))
            .setContentText(context.getText(R.string.message_hello_i_am_notification))
            .build();

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(id++, notification);
    }

}
