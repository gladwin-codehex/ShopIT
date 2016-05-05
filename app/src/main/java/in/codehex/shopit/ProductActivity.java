package in.codehex.shopit;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.codehex.shopit.model.ProductItem;
import in.codehex.shopit.util.AppController;
import in.codehex.shopit.util.Config;
import in.codehex.shopit.util.ItemClickListener;

public class ProductActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    List<ProductItem> productItemList;
    Intent intent;
    RecyclerViewAdapter adapter;
    SwipeRefreshLayout mSwipeRefreshLayout;
    SharedPreferences sharedPreferences;
    String searchTag;
    int shopId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        initObjects();
        prepareObjects();
    }

    /**
     * Initialize the objects.
     */
    private void initObjects() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);

        sharedPreferences = getSharedPreferences(Config.PREF, MODE_PRIVATE);
        productItemList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.product_list);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        adapter = new RecyclerViewAdapter(getApplicationContext(), productItemList);
    }

    /**
     * Implement and manipulate the objects.
     */
    private void prepareObjects() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searchTag = sharedPreferences.getString("searchTag", null);
        shopId = sharedPreferences.getInt("shopId", 0);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.primary_dark,
                R.color.accent);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                getProductList();
            }
        });
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                getProductList();
            }
        });
    }

    /**
     * Fetch the product list from the database.
     */
    private void getProductList() {
        // volley string request to server with POST parameters
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Config.URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                mSwipeRefreshLayout.setRefreshing(false);
                // parsing json response data
                try {
                    JSONArray array = new JSONArray(response);

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);

                        int productId = object.getInt("product_id");
                        String price = object.getString("price");
                        String productName = object.getString("product_name");

                        productItemList.add(new ProductItem(productId, price, productName));
                        adapter.notifyDataSetChanged();
                    }

                    if (productItemList.isEmpty()) {
                        Toast.makeText(getApplicationContext(),
                                "No product is available for the given search tag", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                mSwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getApplicationContext(),
                        "Network error! Check your internet connection!"
                        , Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to the register URL
                Map<String, String> params = new HashMap<>();
                params.put("tag", "product_list");
                params.put("search_tag", searchTag);
                params.put("shop_id", String.valueOf(shopId));

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);
    }

    private class RecyclerViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {
        public TextView textProductName, textProductPrice;
        private ItemClickListener itemClickListener;

        public RecyclerViewHolder(View view) {
            super(view);
            textProductName = (TextView) view.findViewById(R.id.product_name);
            textProductPrice = (TextView) view.findViewById(R.id.product_price);
            view.setOnClickListener(this);
        }

        public void setClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onClick(view, getAdapterPosition(), false);
        }
    }

    private class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {

        private List<ProductItem> productItemList;
        private Context context;

        public RecyclerViewAdapter(Context context, List<ProductItem> productItemList) {
            this.productItemList = productItemList;
            this.context = context;
        }

        @Override
        public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int view) {
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_product, parent, false);
            return new RecyclerViewHolder(layoutView);
        }

        @Override
        public void onBindViewHolder(RecyclerViewHolder holder, int position) {
            final ProductItem productItem = productItemList.get(position);
            String productPrice = "\u20B9 " + productItem.getPrice();
            holder.textProductName.setText(productItem.getProductName());
            holder.textProductPrice.setText(productPrice);

            holder.setClickListener(new ItemClickListener() {
                @Override
                public void onClick(View view, int position, boolean isLongClick) {
                    ProductItem item = productItemList.get(position);
                    int productId = item.getProductId();
                    String productName = item.getProductName();
                    String productPrice = item.getPrice();

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("productName", productName);
                    editor.putString("productPrice", productPrice);
                    editor.putInt("productId", productId);
                    editor.apply();

                    intent = new Intent(getApplicationContext(), ReviewActivity.class);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return this.productItemList.size();
        }
    }
}
