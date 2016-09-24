package com.scoe.gsda;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v4.app.ShareCompat;
import android.support.v4.media.VolumeProviderCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ProductList extends AppCompatActivity {

    private ListView products_list_view;
    private String store_name = "D Mart";
    private myAdapter adapter;
    List<product> product_list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        product_list = new ArrayList<product>();
        products_list_view = (ListView)findViewById(R.id.listView_products_list);
        store_name = getIntent().getStringExtra("store_name");
        new populateOfferList().execute();

    }

    void registerClickCallBack(){
        products_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //AlertDialog alertDialog = new AlertDialog.Builder(ProductList.this).create();
                //alertDialog.setTitle(product_list.get(position).getName());
                //alertDialog.setMessage("Receive updates for this product?");
                AlertDialog.Builder dialog = new AlertDialog.Builder(ProductList.this);
                dialog.setTitle(product_list.get(position).getName());
                dialog.setMessage("Receive updates for this product?");
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();

            }
        });
    }

    private class myAdapter extends ArrayAdapter<product>{

        public myAdapter(Context context,List<product> product_list)
        {
            super(context,R.layout.product_layout,product_list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View product_view = LayoutInflater.from(getContext()).inflate(R.layout.product_layout,parent,false);
            product product_obj = getItem(position);
            TextView tv_name,tv_mrp,tv_offerPrice,tv_percentOff,tv_availability;

            tv_name = (TextView)product_view.findViewById(R.id.textView_product_name);
            tv_mrp = (TextView)product_view.findViewById(R.id.textView_mrp);
            tv_offerPrice = (TextView)product_view.findViewById(R.id.textView_offerPrice);
            tv_percentOff = (TextView)product_view.findViewById(R.id.textView_percentOff);
            tv_availability = (TextView)product_view.findViewById(R.id.textView_availability);

            tv_name.setText(product_obj.getName());
            tv_mrp.setText(product_obj.getMrp_retail());
            tv_offerPrice.setText(product_obj.getMrp_offer());
            tv_percentOff.setText(product_obj.getDiscount());
            tv_availability.setText(product_obj.getAvailability());

            return product_view;
        }
    }
    class product{
        private String name;
        int mrp_retail,discount,mrp_offer,availability;
        public product(String name, int mrp_retail, int discount, int availability) {
            this.name = name;
            this.mrp_retail = mrp_retail;
            this.discount = discount;
            this.availability = availability;
            this.mrp_offer = (mrp_retail - (int)((float)mrp_retail*((float)discount/100)));
        }

        public String getName() {
            return name;
        }

        public String getMrp_retail() {
            return String.valueOf(mrp_retail);
        }

        public String getMrp_offer() {
            return String.valueOf(mrp_offer);
        }

        public String getDiscount() {
            return String.valueOf(discount);
        }

        public String getAvailability() {
            return String.valueOf(availability);
        }
    }

    class populateOfferList extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... arg) {
            try{
                String link = "http://192.168.43.15/gsda/get_offer_list.php?" + "name=" + URLEncoder.encode(store_name,"utf-8");
                URL url = new URL(link);
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String line = null;
                StringBuilder sb = new StringBuilder();

                while ((line = reader.readLine())!= null)
                {
                    sb.append(line);
                }

                return sb.toString();

            }catch (Exception e){
                e.printStackTrace();
                return "nfailure";
            }
        }

        @Override
        protected void onPostExecute(String result) {

            if(result.equals("failure"))
                Toast.makeText(ProductList.this,"Store not found!",Toast.LENGTH_SHORT);
            else {
                try {
                    product_list.clear();
                    JSONObject jobj = new JSONObject(result);
                    JSONArray stuff_array = jobj.getJSONArray("stuff");
                    for(int i=0; i<stuff_array.length();i++)
                    {
                        JSONObject co = new JSONObject();
                        co = stuff_array.getJSONObject(i);
                        product_list.add(new product(co.getString("name"),co.getInt("mrp"),co.getJSONObject("offers").getInt("discount"),co.getInt("stock")));
                    }



                    adapter = new myAdapter(getBaseContext(),product_list);
                    products_list_view.setAdapter(adapter);
                    registerClickCallBack();


                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
