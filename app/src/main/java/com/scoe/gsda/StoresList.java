package com.scoe.gsda;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import java.util.ArrayList;
import java.util.List;

public class StoresList extends AppCompatActivity {

    private myAdapter adapter;
    private List<store> store_list;
    private ListView stores_list_view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stores_list);
        stores_list_view = (ListView)findViewById(R.id.listView_stores);
        store_list = new ArrayList<store>();
        new populateStoreList().execute();
    }

    private class store{
        private int discount;
        private String store_name;

        public store(int discount, String store_name) {
            this.discount = discount;
            this.store_name = store_name;
        }

        public String getDiscount() {
            return String.valueOf(discount);
        }

        public String getStore_name() {
            return store_name;
        }
    }
    private class myAdapter extends ArrayAdapter<store>{

        public myAdapter(Context context, List<store> store_list)
        {
            super(context,R.layout.store_layout,store_list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View store_view = LayoutInflater.from(getContext()).inflate(R.layout.store_layout,parent,false);
            store store_obj = getItem(position);
            TextView tv_storeName, tv_discount;
            tv_storeName = (TextView)store_view.findViewById(R.id.textView_storeName);
            tv_discount = (TextView)store_view.findViewById(R.id.textView_discount);
            tv_storeName.setText(store_obj.getStore_name());
            tv_discount.setText(store_obj.getDiscount());

            return store_view;
        }

    }

    private class populateStoreList extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... params) {
            try {
                String link = "http://192.168.43.15/gsda/get_stores_customer.php";
                URL url = new URL(link);
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write("name=55");
                wr.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String line = null;
                StringBuilder sb = new StringBuilder();
                while((line = reader.readLine())!=null)
                {
                    sb.append(line);
                }

                return sb.toString();

            } catch (Exception e){
                e.printStackTrace();
                return ("nfailure");
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if(s.equals("failure"))
            {
                Toast.makeText(StoresList.this,"Error on server",Toast.LENGTH_SHORT).show();
            }
            else if(s.equals("nfailure"))
            {
                Toast.makeText(StoresList.this,"Network Failure!",Toast.LENGTH_SHORT).show();
            }
            else
            {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    JSONArray stuff_array = jsonObject.getJSONArray("stuff");
                    JSONObject cs = new JSONObject();
                    store_list.clear();
                    for(int i=0; i<stuff_array.length(); i++)
                    {
                        cs = stuff_array.getJSONObject(i);
                        store_list.add(new store(cs.getInt("discount"),cs.getString("name")));

                    }
                    adapter = new myAdapter(getBaseContext(),store_list);
                    stores_list_view.setAdapter(adapter);
                    registerClickCallBack();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    void registerClickCallBack(){
        stores_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                startActivity(new Intent(StoresList.this,ProductList.class).putExtra("store_name",store_list.get(position).getStore_name()));
            }
        });
    }
}
