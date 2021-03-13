package com.hfad.iqtimer;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// Command enumeration
// more info - http://blog.shamanland.com/2016/02/int-string-enum.html
@IntDef({Command.INVALID, Command.STOP, Command.START})
@Retention(RetentionPolicy.SOURCE) @interface Command {

    int INVALID = -1;
    int STOP    = 0;
    int START   = 1;
}


public class MyIntentBuilder{
    private static final String  KEY_MESSAGE = "msg";
    private static final String  KEY_COMMAND = "cmd";
    private              Context mContext;
    private              String  mMessage;
    private @Command     int     mCommandId  = Command.INVALID;

   /* Метод getInstance() называется заводским методом. Он используется для создания класса singleton.
    Это означает, что будет создан только один экземпляр этого класса, а другие получат ссылку на этот класс.*/
    public static MyIntentBuilder getInstance(Context context) {
        return new MyIntentBuilder(context);
    }

    public MyIntentBuilder(Context context) {
        this.mContext = context;
    }

    public MyIntentBuilder setMessage(String message) {
        this.mMessage = message;
        return this;
    }

   public MyIntentBuilder setCommand(@Command int command) {
        this.mCommandId = command;
        return this;
    }

    public Intent build() {
        //Assert.assertNotNull("Context can not be null!", mContext);
        Intent intent = new Intent(mContext, TimerService.class);

        if (mCommandId != Command.INVALID) {
            intent.putExtra(KEY_COMMAND, mCommandId);
        }

        if (mMessage != null) {
            intent.putExtra(KEY_MESSAGE, mMessage);
        }
        return intent;
    }
}
