package io.tripzero.redhouse;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.util.List;

import io.particle.android.sdk.cloud.SparkCloud;
import io.particle.android.sdk.cloud.SparkCloudException;
import io.particle.android.sdk.cloud.SparkDevice;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.Toaster;

import static io.particle.android.sdk.utils.Py.list;

public class MainActivity extends Activity {

    private SparkCloud mSparkCloud;
    private SparkDevice mDevice;
    private String DeviceName = "geohouse";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

    private void connect(String username, String pass)
    {
        Async.executeAsync(SparkCloud.get(getApplicationContext()), new Async.ApiWork<SparkCloud, Void>()
        {
            public Void callApi(SparkCloud sparkCloud) throws SparkCloudException, IOException {
                mSparkCloud = sparkCloud;
                mSparkCloud.logIn("ido@particle.io","l33tp4ssw0rd");
                getDevice();
                return null;
            }

            @Override
            public void onSuccess(Void aVoid) {
                Toaster.l(MainActivity.this, "Logged in");
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
