package ti.miga.tifit;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;

import org.appcelerator.titanium.TiApplication;
import org.appcelerator.kroll.common.Log;
import android.content.Intent;
import android.content.Context;
import android.app.Activity;
import android.os.Bundle;
import android.content.IntentSender;


@Kroll.module(name="Tifit", id="ti.miga.tifit")
public class TifitModule extends KrollModule {

	Context context;
	Activity activity;


	FitActivity fa = new FitActivity();


	public TifitModule() {
		super();
	}

	@Kroll.method
	public void init(){
		Log.i("Noti", "init");
		TiApplication appContext = TiApplication.getInstance();
		activity = appContext.getCurrentActivity();
		context=activity.getApplicationContext();
		fa.buildFitnessClient(activity);
	}

	@Kroll.method
	public void start(){
		fa.onStart();
	}

    @Kroll.method
	public void stop(){
    }

}
