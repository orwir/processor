package ingvar.android.processor.examples.notifier.view;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;

import ingvar.android.processor.examples.R;
import ingvar.android.processor.observation.ScheduledObserver;

/**
 * Created by Igor Zubenko on 2015.07.09.
 */
public class NotifierObserver extends ScheduledObserver<Integer> {

    @Override
    public void completed(Integer result) {
        Context context = getContext();
        Intent intent = new Intent(context, NotifierActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(NotifierActivity.class);
        stackBuilder.addNextIntent(intent);

        Notification notification = new NotificationCompat.Builder(context)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getText(R.string.label_dummy_notification))
            .setContentText(context.getString(R.string.message_hello_i_am_notification, result))
            .setContentIntent(stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT))
            .build();

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(result > 0) {
            manager.cancel(result-1);
        }
        manager.notify(result, notification);
    }

    @Override
    public void failed(Exception exception) {
        Toast.makeText(getContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
    }

}
