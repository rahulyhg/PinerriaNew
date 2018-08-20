package pinerria.business.pinerrianew.Activites;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ebs.android.sdk.Config;
import com.ebs.android.sdk.EBSPayment;
import com.ebs.android.sdk.PaymentRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import pinerria.business.pinerrianew.Fragments.PayOrder;
import pinerria.business.pinerrianew.R;
import pinerria.business.pinerrianew.Utils.Api;
import pinerria.business.pinerrianew.Utils.MyPrefrences;
import pinerria.business.pinerrianew.Utils.Util;

public class PayOrderAct extends AppCompatActivity {
    Dialog dialog;
    TextView paymentId,amount,descreption;
    Button submit;
    CheckBox checkBob;
    String  flag="false";
    EditText oder_id,user_email,tax_address,gst_number,company_name;

    ArrayList<HashMap<String, String>> custom_post_parameters;
    private static final int ACC_ID = 27791;// Provided by EBS
    private static final String SECRET_KEY = "87a9449095742721db3814e444495e9b";// Provided by EBS


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_order);

        amount=findViewById(R.id.amount);
        descreption=findViewById(R.id.descreption);
        paymentId=findViewById(R.id.paymentId);
        submit= findViewById(R.id.submit);
        checkBob= findViewById(R.id.checkBob);


        company_name=findViewById(R.id.company_name);
        gst_number=findViewById(R.id.gst_number);
        tax_address=findViewById(R.id.tax_address);
        user_email=findViewById(R.id.user_email);


        dialog=new Dialog(PayOrderAct.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        // Util.showPgDialog(dialog);

        Log.d("sdfsdfsdfs",getIntent().getStringExtra("jsonArray"));

        checkBob.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
              //  Toast.makeText(getApplicationContext(), ""+b, Toast.LENGTH_SHORT).show();

                if (b==true){
                   flag="true";
                    gst_number.setText("N/A");
                    gst_number.setFocusable(false);

                }
                else if(b==false){
                    flag="false";
                    gst_number.setText("");
                    gst_number.setFocusable(true);
                    gst_number.setFocusableInTouchMode(true);
                }
            }
        });



        try {
           JSONArray jsonArray=new JSONArray(getIntent().getStringExtra("jsonArray"));

           final JSONObject jsonObject=jsonArray.getJSONObject(0);

            amount.setText("Amount ₹ "+jsonObject.optString("amount"));
            descreption.setText(jsonObject.optString("description"));
            paymentId.setText("Transaction No: "+jsonObject.optString("payment_id"));

            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (validate()) {


                        Long tsLong = System.currentTimeMillis()/1000;
                        String ts = tsLong.toString();
                        Log.d("TimeCurrent",ts);
                        MyPrefrences.setDateTime(getApplicationContext(),ts);


                        submitOrderApi(jsonObject.optString("payment_id"),jsonObject.optString("amount"));

                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private void submitOrderApi(final String payment_id, final String p_amt) {

            Util.showPgDialog(dialog);
            RequestQueue queue = Volley.newRequestQueue(PayOrderAct.this);
            StringRequest strReq = new StringRequest(Request.Method.POST,
                    Api.payOrderAmount, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Util.cancelPgDialog(dialog);
                    Log.e("dfsjfdfsdfgd", "Login Response: " + response);

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                         if (jsonObject.getString("status").equalsIgnoreCase("success")) {

                             callEbsKit(PayOrderAct.this, Double.parseDouble(p_amt));

                         }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Util.cancelPgDialog(dialog);
                    Log.e("fdgdfgdfgd", "Login Error: " + error.getMessage());
                    Toast.makeText(getApplicationContext(), "Please Connect to the Internet or Wrong Password", Toast.LENGTH_LONG).show();
                }
            }) {

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Log.e("fgdfgdfgdf", "Inside getParams");

                    // Posting parameters to login url
                    Map<String, String> params = new HashMap<>();
                    params.put("payment_id", payment_id);

                            Random r = new Random();
                            int i1 = r.nextInt(800 - 650) + 65;
                            Log.d("fgsdgfsdghdfhd", String.valueOf(i1));

                    params.put("transaction_id", String.valueOf(i1));
                    params.put("payment_status", "success");
                    params.put("company_name", company_name.getText().toString());
                    if (flag.equals("true")){
                        params.put("gst_number", "NA");
                    }
                    else if (flag.equals("false")){
                        params.put("gst_number", gst_number.getText().toString());

                    }

                    params.put("tax_address", tax_address.getText().toString());
                    params.put("user_email", user_email.getText().toString());
                    params.put("unique_number", MyPrefrences.getDateTime(getApplicationContext()));

                    return params;
                }

//                        @Override
//                        public Map<String, String> getHeaders() throws AuthFailureError {
//                            Log.e("fdgdfgdfgdfg","Inside getHeaders()");
//                            Map<String,String> headers=new HashMap<>();
//                            headers.put("Content-Type","application/x-www-form-urlencoded");
//                            return headers;
//                        }
            };
            // Adding request to request queue
            queue.add(strReq);


    }

    private boolean validate(){

        if (TextUtils.isEmpty(company_name.getText().toString()))
        {
            Util.errorDialog(PayOrderAct.this,"Type Company Name");
            return false;
        }


//        else if (TextUtils.isEmpty(gst_number.getText().toString())) {
//            if (flag.equals("true")) {
//                Util.errorDialog(PayOrderAct.this, "GST No ");
//                return false;
//            }
//            else{
//                return true;
//            }
//        }

        else if (TextUtils.isEmpty(tax_address.getText().toString()))
        {
            Util.errorDialog(PayOrderAct.this,"Type Address");
            return false;
        }  else if (TextUtils.isEmpty(user_email.getText().toString()))
        {
            Util.errorDialog(PayOrderAct.this,"Type Email Id");
            return false;
        }


        return true;

    }



    private void callEbsKit(PayOrderAct buyProduct, double amount) {
        /**
         * Set Parameters Before Initializing the EBS Gateway, All mandatory
         * values must be provided
         */

        /** Payment Amount Details */
        // Total Amount

        PaymentRequest.getInstance().setTransactionAmount(String.valueOf(amount));

        /** Mandatory */

        PaymentRequest.getInstance().setAccountId(ACC_ID);
        PaymentRequest.getInstance().setSecureKey(SECRET_KEY);

        // Reference No
//        Random r = new Random();
//        int i1 = r.nextInt(800 - 650) + 65;
//        Log.d("fgsdgfsdghdfhd", String.valueOf(i1));

        Random r = new Random();
        int i1 = r.nextInt(800 - 650) + 65;
        Log.d("fgsdgfsdghdfhd", String.valueOf(i1));
        PaymentRequest.getInstance().setReferenceNo(String.valueOf(i1));
        /** Mandatory */

        // Email Id
        //PaymentRequest.getInstance().setBillingEmail("test_tag@testmail.com");

        PaymentRequest.getInstance().setBillingEmail("customerhelpdesk@pinerria.com");
        /** Mandatory */

        PaymentRequest.getInstance().setFailureid(String.valueOf(amount));

        // PaymentRequest.getInstance().setFailuremessage(getResources().getString(R.string.payment_failure_message));
        // System.out.println("FAILURE MESSAGE"+getResources().getString(R.string.payment_failure_message));

        /** Mandatory */

        // Currency
        PaymentRequest.getInstance().setCurrency("INR");
        /** Mandatory */

        /** Optional */
        // Your Reference No or Order Id for this transaction
        PaymentRequest.getInstance().setTransactionDescription(
                "Test Transaction");

        /** Billing Details */
        PaymentRequest.getInstance().setBillingName("Type");
        /** Optional */
        PaymentRequest.getInstance().setBillingAddress("North Mada Street");
        /** Optional */
        PaymentRequest.getInstance().setBillingCity("Chennai");
        /** Optional */
        PaymentRequest.getInstance().setBillingPostalCode("600019");
        /** Optional */
        PaymentRequest.getInstance().setBillingState("Tamilnadu");
        /** Optional */
        PaymentRequest.getInstance().setBillingCountry("IND");
        /** Optional */
        PaymentRequest.getInstance().setBillingPhone("01234567890");
        /** Optional */

        /** Shipping Details */
        PaymentRequest.getInstance().setShippingName("Test_Name");
        /** Optional */
        PaymentRequest.getInstance().setShippingAddress("North Mada Street");
        /** Optional */
        PaymentRequest.getInstance().setShippingCity("Chennai");
        /** Optional */
        PaymentRequest.getInstance().setShippingPostalCode("600019");
        /** Optional */
        PaymentRequest.getInstance().setShippingState("Tamilnadu");
        /** Optional */
        PaymentRequest.getInstance().setShippingCountry("IND");
        /** Optional */
        PaymentRequest.getInstance().setShippingEmail("test@testmail.com");
        /** Optional */
        PaymentRequest.getInstance().setShippingPhone(MyPrefrences.getDateTime(getApplicationContext()));
        /** Optional */

        PaymentRequest.getInstance().setLogEnabled(String.valueOf(amount));


        /**
         * Payment option configuration
         */

        /** Optional */
        PaymentRequest.getInstance().setHidePaymentOption(false);

        /** Optional */
        PaymentRequest.getInstance().setHideCashCardOption(false);

        /** Optional */
        PaymentRequest.getInstance().setHideCreditCardOption(false);

        /** Optional */
        PaymentRequest.getInstance().setHideDebitCardOption(false);

        /** Optional */
        PaymentRequest.getInstance().setHideNetBankingOption(false);

        /** Optional */
        PaymentRequest.getInstance().setHideStoredCardOption(false);

        /**
         * Initialise parameters for dyanmic values sending from merchant
         */

        custom_post_parameters = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> hashpostvalues = new HashMap<String, String>();
        hashpostvalues.put("account_details", "saving");
        hashpostvalues.put("merchant_type", "gold");
        custom_post_parameters.add(hashpostvalues);

        PaymentRequest.getInstance()
                .setCustomPostValues(custom_post_parameters);
        /** Optional-Set dyanamic values */

        // PaymentRequest.getInstance().setFailuremessage(getResources().getString(R.string.payment_failure_message));

        EBSPayment.getInstance().init(PayOrderAct.this, ACC_ID, SECRET_KEY,
                Config.Mode.ENV_LIVE, Config.Encryption.ALGORITHM_SHA512, "EBS");

    }



}
