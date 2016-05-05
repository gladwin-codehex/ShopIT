package in.codehex.shopit;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.codehex.shopit.model.ReviewItem;
import in.codehex.shopit.util.AppController;
import in.codehex.shopit.util.Config;

public class ReviewActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextView textProductName, textProductPrice;
    RatingBar ratingBar;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    ProgressDialog progressDialog;
    List<ReviewItem> reviewItemList;
    Intent intent;
    RecyclerViewAdapter adapter;
    SharedPreferences sharedPreferences, favorite;
    FloatingActionButton fab;
    String productName, productPrice;
    int productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        initObjects();
        prepareObjects();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_review, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_favorite) {
            SharedPreferences.Editor editor = favorite.edit();
            editor.putString(productName, productName);
            editor.apply();
            Toast.makeText(getApplicationContext(),
                    "Added to favorites", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Initialize the objects.
     */
    private void initObjects() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        textProductName = (TextView) findViewById(R.id.product_name);
        textProductPrice = (TextView) findViewById(R.id.product_price);
        ratingBar = (RatingBar) findViewById(R.id.rating);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        sharedPreferences = getSharedPreferences(Config.PREF, MODE_PRIVATE);
        favorite = getSharedPreferences(Config.FAVORITE, MODE_PRIVATE);
        progressDialog = new ProgressDialog(this);
        reviewItemList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.review_list);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        adapter = new RecyclerViewAdapter(getApplicationContext(), reviewItemList);
    }

    /**
     * Implement and manipulate the objects.
     */
    private void prepareObjects() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        productName = sharedPreferences.getString("productName", null);
        productPrice = "\u20B9 " + sharedPreferences.getString("productPrice", null);
        productId = sharedPreferences.getInt("productId", 0);

        textProductName.setText(productName);
        textProductPrice.setText(productPrice);
        ratingBar.setRating(1.0f);

        progressDialog.setMessage("Loading reviews..");
        progressDialog.setCancelable(false);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(getApplicationContext(), FeedbackActivity.class);
                startActivity(intent);
            }
        });
        getReviewList();
    }

    /**
     * Fetch the reviews of all the users for the given product.
     */
    private void getReviewList() {
        showProgressDialog();
        // volley string request to server with POST parameters
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Config.URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideProgressDialog();
                // parsing json response data
                int ratingCount = 0;
                double totalRating = 0.0;
                try {
                    JSONArray array = new JSONArray(response);

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);

                        double rating = object.getDouble("rating");
                        String feedback = object.getString("feedback");

                        totalRating += rating;
                        ratingCount++;

                        reviewItemList.add(new ReviewItem(rating, feedback));
                        adapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                float rating = (float) (totalRating / ratingCount);
                ratingBar.setRating(rating);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                hideProgressDialog();
                Toast.makeText(getApplicationContext(),
                        "Network error! Check your internet connection!",
                        Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to the register URL
                Map<String, String> params = new HashMap<>();
                params.put("tag", "review_list");
                params.put("product_id", String.valueOf(productId));

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);
    }

    /**
     * Display progress bar if it is not being shown.
     */
    private void showProgressDialog() {
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    /**
     * Hide progress bar if it is being displayed.
     */
    private void hideProgressDialog() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private class RecyclerViewHolder extends RecyclerView.ViewHolder {
        public TextView textFeedback;

        public RecyclerViewHolder(View view) {
            super(view);
            textFeedback = (TextView) view.findViewById(R.id.feedback);
        }
    }

    private class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {

        private List<ReviewItem> reviewItemList;
        private Context context;

        public RecyclerViewAdapter(Context context, List<ReviewItem> reviewItemList) {
            this.reviewItemList = reviewItemList;
            this.context = context;
        }

        @Override
        public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int view) {
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_review, parent, false);
            return new RecyclerViewHolder(layoutView);
        }

        @Override
        public void onBindViewHolder(RecyclerViewHolder holder, int position) {
            final ReviewItem reviewItem = reviewItemList.get(position);
            holder.textFeedback.setText(reviewItem.getFeedback());

        }

        @Override
        public int getItemCount() {
            return this.reviewItemList.size();
        }
    }
}
