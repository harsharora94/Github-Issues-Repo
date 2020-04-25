package com.example.harsh.testdemo;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.harsh.testdemo.http.api.JGetWebService;
import com.example.harsh.testdemo.http.api.JSONWebServiceResponse;
import com.example.harsh.testdemo.models.Comment;
import com.example.harsh.testdemo.models.Issue;
import com.example.harsh.testdemo.utils.NetworkUtils;
import com.example.harsh.testdemo.utils.ProgressBarUtil;
import com.example.harsh.testdemo.utils.StringUtils;
import com.example.harsh.testdemo.widgets.CommentDialog;
import com.example.harsh.testdemo.widgets.Org_Repo_Dialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String SCHEME = "https";
    private static final String HOST = "api.github.com";
    private static final String REPOS = "repos";
    private static final String ORG_NAME = "rails";
    private static final String REPO_NAME = "rails";
    private static final String ISSUES = "issues";

    private IssueAdapter issueAdapter;
    private ArrayList<Issue> issueList;
    private ArrayList<Comment> commentList;
    private ProgressBarUtil progressBar;
    private AlertDialog alertDialog;
    private LinearLayout retryLayout;
    ListView listView;
    FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = new ProgressBarUtil(this);

        floatingActionButton= (FloatingActionButton)findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Org_Repo_Dialog alertDialogFragment = new Org_Repo_Dialog();
                alertDialogFragment.show(getFragmentManager(), "view_org_repo_dialog");
            }
        });

        issueList = new ArrayList<Issue>();
        commentList = new ArrayList<Comment>();

        listView = (ListView) findViewById(R.id.listview_issues);
        retryLayout = (LinearLayout) findViewById(R.id.retry_linearLayout);
        Button retryButton = (Button) findViewById(R.id.button_retry);
        retryButton.setOnClickListener(this);
        issueAdapter = new IssueAdapter(this, issueList);
        listView.setAdapter(issueAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                commentList.clear();
                Issue issue = issueList.get(position);
                int noOfComments = issue.getComments();

                if (noOfComments > 0) {
                    if (NetworkUtils.isOnline(MainActivity.this)) {
                        FetchCommentTask fetchCommentTask = new FetchCommentTask(MainActivity.this);
                        fetchCommentTask.execute(String.valueOf(issue.getNumber()));
                    } else {
                        Toast.makeText(MainActivity.this, getString(R.string.internet_con), Toast.LENGTH_LONG).show();
                    }

                } else {
                    showCommentsForIssueDialog();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchIssueDataIfOnline();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_retry:
                fetchIssueDataIfOnline();
                break;
        }
    }

    private void fetchIssueDataIfOnline() {
        if (NetworkUtils.isOnline(this)) {
            retryLayout.setVisibility(View.GONE);
            if (issueList.isEmpty()) {
                FetchOpenIssueTask fetchOpenIssueTask = new FetchOpenIssueTask(this);
                fetchOpenIssueTask.execute();
            }
        } else {
            if (issueList.isEmpty()) {
                retryLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private class IssueUpdatedAtComparator implements Comparator<Issue> {
        @Override
        public int compare(Issue issue1, Issue issue2) {
            return issue2.getUpdatedAt().compareTo(issue1.getUpdatedAt());
        }
    }

    private class FetchOpenIssueTask extends AsyncTask<String, Void, JSONWebServiceResponse> {

        private final String LOG_TAG = FetchOpenIssueTask.class.getSimpleName();
        private JSONWebServiceResponse response;
        private final JGetWebService jsonGetWS;
        private final Context context;
        private boolean isApiHit = false;
        private final int noOfChars = 140;

        public FetchOpenIssueTask(Context context) {
            super();
            this.response = new JSONWebServiceResponse(new JSONArray(),
                    HttpURLConnection.HTTP_NO_CONTENT);
            this.jsonGetWS = new JGetWebService();
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            progressBar.show();
        }

        @Override
        protected JSONWebServiceResponse doInBackground(String... params) {
            Log.d(LOG_TAG, "doInBackground started");

            if (NetworkUtils.isOnline(context)) {

                final String STATE_PARAM = "state";
                String state = "open";

                Uri.Builder builder = new Uri.Builder();
                builder.scheme(SCHEME)
                        .authority(HOST)
                        .appendPath(REPOS)
                        .appendPath(ORG_NAME)
                        .appendPath(REPO_NAME)
                        .appendPath(ISSUES)
                        .appendQueryParameter(STATE_PARAM, state);
                Uri builtUri = builder.build();

                try {
                    URL url = new URL(builtUri.toString());
                    Log.v(LOG_TAG, "Built Uri : " + builtUri.toString());

                    response = jsonGetWS.hit(url);
                    isApiHit = true;
                } catch (MalformedURLException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                }
            }

            Log.d(LOG_TAG, "doInBackground completed");
            return response;
        }

        public void getIssueDataFromJsonIntoIssueList(JSONArray jsonArrayResponse) {

            issueList.clear();
            Issue issue;

            for (int i = 0; i < jsonArrayResponse.length(); i++) {
                try {
                    JSONObject jsonObject = jsonArrayResponse.getJSONObject(i);
                    String title = jsonObject.getString("title");
                    String updatedAt = jsonObject.getString("updated_at");
                    String createdAt = jsonObject.getString("created_at");
                    String body = jsonObject.getString("body");
                    int number = jsonObject.getInt("number");
                    int comments = jsonObject.getInt("comments");

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date date = sdf.parse(updatedAt);

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date date1 = simpleDateFormat.parse(createdAt);

                    issue = new Issue();
                    issue.setTitle(title);
                    issue.setUpdatedAt(date);
                    issue.setCreatedAt(date1);
                    issue.setNumber(number);
                    issue.setComments(comments);

                    if (StringUtils.isNotEmpty(body) && body.length() > noOfChars) {
                        issue.setBody(body.substring(0, noOfChars));
                    } else {
                        issue.setBody("");
                    }
                    issueList.add(issue);

                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                } catch (ParseException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                }
            }
        }

        @Override
        protected void onPostExecute(JSONWebServiceResponse response) {

            progressBar.hide();
            if (response != null && response.getCode() == HttpURLConnection.HTTP_OK) {
                if (isApiHit) {
                    JSONArray jsonArrayResponse = response.getResult();
                    getIssueDataFromJsonIntoIssueList(jsonArrayResponse);

                    Collections.sort(issueList, new IssueUpdatedAtComparator());
                    issueAdapter.notifyDataSetChanged();

                } else {
                    Toast.makeText(context, getString(R.string.internet_con), Toast.LENGTH_LONG).show();
                }
            } else {
                showErrorDialog(getString(R.string.sorry_header), getString(R.string.generic_failure_desc));
            }
        }
    }

    private class FetchCommentTask extends AsyncTask<String, Void, JSONWebServiceResponse> {

        private final String LOG_TAG = FetchCommentTask.class.getSimpleName();
        private JSONWebServiceResponse response;
        private final JGetWebService jsonGetWS;
        private final Context context;
        private boolean isApiHit = false;

        public FetchCommentTask(Context context) {
            super();
            this.response = new JSONWebServiceResponse(new JSONArray(), HttpURLConnection.HTTP_NO_CONTENT);
            this.jsonGetWS = new JGetWebService();
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            progressBar.show();
        }

        @Override
        protected JSONWebServiceResponse doInBackground(String... params) {
            Log.d(LOG_TAG, "doInBackground started");

            if (NetworkUtils.isOnline(context)) {

                final String issueNo = params[0];

                Uri.Builder builder = new Uri.Builder();
                builder.scheme(SCHEME)
                        .authority(HOST)
                        .appendPath(REPOS)
                        .appendPath(ORG_NAME)
                        .appendPath(REPO_NAME)
                        .appendPath(ISSUES)
                        .appendPath(issueNo)
                        .appendPath("comments");
                Uri builtUri = builder.build();
                try
                {
                    URL url = new URL(builtUri.toString());
                    Log.v(LOG_TAG, "Built Uri : " + builtUri.toString());

                    response = jsonGetWS.hit(url);
                    isApiHit = true;
                }
                catch (MalformedURLException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                }
            }
            Log.d(LOG_TAG, "doInBackground completed");
            return response;
        }

        private void getCommentDataFromJsonIntoCommentList(JSONArray jsonArrayResponse)
        {
            commentList.clear();
            Comment comment;
            for (int i = 0; i < jsonArrayResponse.length(); i++) {
                try {
                    JSONObject jsonObject = jsonArrayResponse.getJSONObject(i);
                    String user = jsonObject.getJSONObject("user").getString("login");
                    String body = jsonObject.getString("body");

                    comment = new Comment();
                    comment.setUser(user);
                    comment.setBody(body);
                    commentList.add(comment);
                }
                catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                }
            }
        }

        @Override
        protected void onPostExecute(JSONWebServiceResponse response) {

            progressBar.hide();
            if (response != null && response.getCode() == HttpURLConnection.HTTP_OK) {
                if (isApiHit) {
                    JSONArray jsonArrayResponse = response.getResult();
                    getCommentDataFromJsonIntoCommentList(jsonArrayResponse);
                    showCommentsForIssueDialog();
                } else {
                    Toast.makeText(context, getString(R.string.internet_con),
                            Toast.LENGTH_LONG).show();
                }
            } else {
                showErrorDialog(getString(R.string.sorry_header),
                        getString(R.string.generic_failure_desc));
            }
        }
    }

    private void showCommentsForIssueDialog() {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        CommentDialog mCommentDialog = CommentDialog.newInstance(this, commentList);
        mCommentDialog.show(fragmentTransaction, "CommentDialog");
    }

    private void showErrorDialog(String header, String message) {
        if (alertDialog == null || !alertDialog.isShowing()) {

            View contentView = LayoutInflater.from(this).inflate(R.layout.view_message_dialog, null, false);
            alertDialog = new AlertDialog.Builder(this).setView(contentView).create();
            ((TextView) contentView.findViewById(R.id.item_header)).setText(header);
            ((TextView) contentView.findViewById(R.id.item_message)).setText(message);

            contentView.findViewById(R.id.button_close).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.setCancelable(false);
            alertDialog.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.created:

                Collections.sort(issueList, new Comparator<Issue>(){
                    public int compare(Issue issue1, Issue issue2) {
                        return issue1.getCreatedAt().toString().compareToIgnoreCase(issue2.getCreatedAt().toString());
                    }
                });
                issueAdapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "Sort by created successfully done...", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.updated:

                Collections.sort(issueList, new Comparator<Issue>(){
                    public int compare(Issue issue1, Issue issue2) {
                        return issue1.getUpdatedAt().toString().compareToIgnoreCase(issue2.getUpdatedAt().toString());
                    }
                });
                issueAdapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "Issue list updated...", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.comments:

                Collections.sort(commentList, new Comparator<Comment>(){
                    public int compare(Comment c1, Comment c2) {
                        return c1.getBody().compareToIgnoreCase(c2.getBody());
                   }
                });
                Toast.makeText(getApplicationContext(), "Sort by comments done...", Toast.LENGTH_SHORT).show();
            return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}