package io.tripzero.redhouse;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;

import java.io.IOException;
import java.util.List;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;
import io.codetail.animation.arcanimator.ArcAnimator;
import io.codetail.animation.arcanimator.Side;
import io.codetail.widget.RevealFrameLayout;
import io.particle.android.sdk.cloud.SparkCloud;
import io.particle.android.sdk.cloud.SparkCloudException;
import io.particle.android.sdk.cloud.SparkDevice;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.Toaster;

import static io.particle.android.sdk.utils.Py.list;

public class MainActivity extends AppCompatActivity {

    private SparkCloud mSparkCloud;
    private SparkDevice mDevice;
    private String DeviceName = "geohouse";
    float startX,startY;
    int endX,endY;

    ArcAnimator arcAnimator;
    Animation revealAnim,fadeOut;
    final static AccelerateInterpolator ACCELERATE = new AccelerateInterpolator();
    EditText username,password;
    FloatingActionButton loginFab;
    RevealFrameLayout revealFrameLayout;
    FrameLayout layoutFrame;
    View mReveal,emptyView;
    CollapsingToolbarLayout collapsingToolbarLayout;
    Toolbar toolbar;
    ImageView coverImage;
    CoordinatorLayout cLayout;
    SwitchCompat waterSwitch;
    TextView soilTempTitle,soilMoistTitle,airMoistTitle,soilTemp,soilMoist,airTemp,waterTitle,soilTitle,airTitle,emptyTitle,emptySubtitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        /**
         ******************************************
         ***********INSTANIATING VIEWS*************
         ******************************************
         **/
        final SharedPreferences.Editor prefs = getSharedPreferences("WaterOn", MODE_PRIVATE).edit();
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        loginFab = (FloatingActionButton) findViewById(R.id.login_fab);
        coverImage = (ImageView) findViewById(R.id.cover_image);
        revealFrameLayout = (RevealFrameLayout) findViewById(R.id.reveal_frame);
        layoutFrame = (FrameLayout) findViewById(R.id.layout_frame);
        mReveal = findViewById(R.id.reveal);
        emptyView = findViewById(R.id.empty_view);
        soilTempTitle = (TextView) findViewById(R.id.soil_temp_title);
        soilMoistTitle = (TextView) findViewById(R.id.soil_moist_title);
        airMoistTitle = (TextView) findViewById(R.id.air_moist_title);
        soilTemp = (TextView) findViewById(R.id.soil_temp);
        soilMoist = (TextView) findViewById(R.id.soil_moist);
        airTemp = (TextView) findViewById(R.id.air_temp);
        soilTitle = (TextView) findViewById(R.id.soil_title);
        airTitle = (TextView) findViewById(R.id.air_title);
        waterTitle = (TextView) findViewById(R.id.water_title);
        emptySubtitle = (TextView) findViewById(R.id.subtitle);
        emptyTitle = (TextView) findViewById(R.id.title);
        waterSwitch = (SwitchCompat) findViewById(R.id.water_switch);
        cLayout = (CoordinatorLayout) findViewById(R.id.main_content);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        revealAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in_slow);
        fadeOut = AnimationUtils.loadAnimation(this,R.anim.fade_out);
        toolbar = (Toolbar) findViewById(R.id.toolbar);


        /**
         ******************************************
         *************DECORATE VIEWS***************
         ******************************************
         **/

        Typeface tf1 = Typeface.createFromAsset(this.getAssets(), "fonts/Effra_Std_Rg.otf");
        Typeface tf2 = Typeface.createFromAsset(this.getAssets(), "fonts/Effra_Std_Md.otf");
        Typeface tf3 = Typeface.createFromAsset(this.getAssets(), "fonts/Effra_Std_Bd.otf");
        emptyTitle.setTypeface(tf3);
        emptySubtitle.setTypeface(tf1);
        soilTempTitle.setTypeface(tf2);
        soilMoistTitle.setTypeface(tf2);
        airMoistTitle.setTypeface(tf2);
        waterTitle.setTypeface(tf3);
        airTitle.setTypeface(tf3);
        soilTitle.setTypeface(tf3);
        soilTemp.setTypeface(tf1);
        airTemp.setTypeface(tf1);
        soilMoist.setTypeface(tf1);

        collapsingToolbarLayout.setTitle("Redhouse");
        collapsingToolbarLayout.setExpandedTitleColor(Color.TRANSPARENT);

        emptyView.setVisibility(View.VISIBLE);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.primary_dark));
        }
        /**
         ******************************************
         ***********BIND DATA TO VIEWS*************
         ******************************************
         **/



        /**
         ********************************************
         ***********HANDLE BUTTON CLICKS*************
         ********************************************
         **/

        loginFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String user = username.getText().toString();
                // or chars or whatever for password
                String pass = password.getText().toString();
              //  connect(user, pass);
                ArcAnimation();
              // Comment or delete ArcAnimation(); when ready to enter credentials then uncomment connect(user,pass);


            }
        });
        final SharedPreferences waterPrefs = getSharedPreferences("WaterOn",0);
        waterSwitch.setChecked(waterPrefs.getBoolean("waterSwitch",false));
        waterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){

                    prefs.putBoolean("waterSwitch", true);
                    prefs.commit();

                 //   waterOn();
                } else {

                    prefs.putBoolean("waterSwitch", false);
                    prefs.commit();
                 //   waterOff();
                }
            }
        });


    }

    /**
     ******************************************
     ***************ANIMNATIONS****************
     ******************************************
     **/

    public void ArcAnimation(){


        startX = Utils.centerX(loginFab);
        startY = Utils.centerY(loginFab);

        endX = revealFrameLayout.getRight() / 2;
        endY = (int) (revealFrameLayout.getBottom() * 0.8f);
        arcAnimator = ArcAnimator.createArcAnimator(loginFab, endX,
                endY, -90, Side.RIGHT)
                .setDuration(500);
        arcAnimator.addListener(new SimpleListener(){

            @Override
            public void onAnimationEnd(Animator animation) {
                CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) loginFab.getLayoutParams();
                p.setBehavior(null); //should disable default animations
                p.setAnchorId(View.NO_ID); //should let you set visibility
                loginFab.setLayoutParams(p);

                RevealData();
            }
        });
        arcAnimator.start();


    }

    public void RevealData(){

        mReveal.setVisibility(View.VISIBLE);
        float finalRadius = Math.max(layoutFrame.getWidth(), layoutFrame.getHeight());

        SupportAnimator animator = ViewAnimationUtils.createCircularReveal(mReveal, endX, (endY / 2) - 212, loginFab.getWidth() / 2f,
                finalRadius);
        animator.setDuration(500);
        animator.setInterpolator(ACCELERATE);
        animator.addListener(new SimpleListener(){
            @Override
            public void onAnimationEnd() {


                coverImage.startAnimation(revealAnim);
                coverImage.setVisibility(View.VISIBLE);
                loginFab.setVisibility(View.GONE);


                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {


                        mReveal.setVisibility(View.INVISIBLE);
                        emptyView.setVisibility(View.INVISIBLE);

                    }
                }, 400);

            }
        });
        animator.start();



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void connect(final String username, final String pass)
    {
        Async.executeAsync(SparkCloud.get(getApplicationContext()), new Async.ApiWork<SparkCloud, Void>()
        {
            public Void callApi(SparkCloud sparkCloud) throws SparkCloudException, IOException {
                mSparkCloud = sparkCloud;
                mSparkCloud.logIn(username,pass);
              //  mSparkCloud.logIn("ido@particle.io","l33tp4ssw0rd");
                getDevice();
                return null;
            }

            @Override
            public void onSuccess(Void aVoid) {
                Toaster.l(MainActivity.this, "Logged in");
                ArcAnimation();
            }

            @Override
            public void onFailure(SparkCloudException e) {
                Toaster.l(MainActivity.this, "Wrong credentials or no internet connectivity, please try again");
            }
        });
    }

    private void getDevice()
    {
        Async.executeAsync(mSparkCloud, new Async.ApiWork<SparkCloud, List<SparkDevice>>() {

            public List<SparkDevice> callApi(SparkCloud sparkCloud) throws SparkCloudException, IOException {
                return sparkCloud.getDevices();
            }

            @Override
            public void onSuccess(List<SparkDevice> devices) {
                for (SparkDevice device : devices) {
                    if (device.getName().equals(DeviceName)) {
                        mDevice = device;
                        return;
                    }
                }
            }

            @Override
            public void onFailure(SparkCloudException e) {
                Log.e("SOME_TAG", String.valueOf(e));
                Toaster.l(MainActivity.this, "Wrong credentials or no internet connectivity, please try again");
            }
        });
    }

    /// TODO: make this generic <T> when ParticleDevice supports generics
    private void getVariable(final String variableName)
    {
        Async.executeAsync(mDevice, new Async.ApiWork<SparkDevice, Integer>() {

            public Integer callApi(SparkDevice sparkDevice) throws SparkCloudException, IOException {
                try {
                    return sparkDevice.getVariable(variableName);
                } catch (SparkDevice.VariableDoesNotExistException e) {
                    e.printStackTrace();
                }
                return 0;
            }

            @Override
            public void onSuccess(Integer value) {
                Toaster.s(MainActivity.this, variableName + " is " + value);
            }

            @Override
            public void onFailure(SparkCloudException e) {
                Log.e("SOME_TAG", String.valueOf(e));
                Toaster.l(MainActivity.this, "Wrong credentials or no internet connectivity, please try again");
            }
        });
    }

    private void callFunction(final String functionName, final String args)
    {
        Async.executeAsync(mDevice, new Async.ApiWork<SparkDevice, Integer>() {

            public Integer callApi(SparkDevice sparkDevice) throws SparkCloudException, IOException {
                try {
                    return sparkDevice.callFunction(functionName, list(args));
                } catch (SparkDevice.FunctionDoesNotExistException e) {
                    e.printStackTrace();
                }
                return 0;
            }

            @Override
            public void onSuccess(Integer returnValue) {
                Toaster.s(MainActivity.this, functionName + " successfully called");
            }

            @Override
            public void onFailure(SparkCloudException e) {
                Log.e("SOME_TAG", String.valueOf(e));
            }
        });
    }

    private void waterOn()
    {
        callFunction("waterOn", "");
    }

    private void waterOff()
    {
        callFunction("waterOff", "");
    }
}
