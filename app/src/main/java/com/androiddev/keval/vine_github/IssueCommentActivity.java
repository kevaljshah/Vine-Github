package com.androiddev.keval.vine_github;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Keval on 3/8/16.
 */
public class IssueCommentActivity extends AppCompatActivity {

    String commenturl;
    ListView listComments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list_comments);

        Intent i = getIntent();
        commenturl = i.getExtras().getString("commentsurl");

        listComments = (ListView)findViewById(R.id.listComments);
        new JSONParseObject().execute(commenturl);


    }

    public class JSONParseObject extends AsyncTask<String, String, List<CommentModel> > {

        @Override
        protected List<CommentModel> doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                String JSONread = buffer.toString();

                List<CommentModel> commentModelList = new ArrayList<>();

                JSONArray json = new JSONArray(JSONread);

                for (int i = 0; i < json.length(); i++) {
                    JSONObject issueObject = json.getJSONObject(i);
                    JSONObject user = issueObject.getJSONObject("user");

                    CommentModel commentModel = new CommentModel();

                    commentModel.setCommentsBody(issueObject.getString("body"));
                    commentModel.setUsername(user.getString("login"));

                    commentModelList.add(commentModel);
                }

                return commentModelList;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<CommentModel> result) {
            super.onPostExecute(result);

            CommentAdapter cAdapter = new CommentAdapter(getApplicationContext(), R.layout.comments_list_view_layout, result);
            listComments.setAdapter(cAdapter);

        }
    }


    public class CommentAdapter extends ArrayAdapter
    {
        private List<CommentModel> commentModelList = new ArrayList<>();
        private int resource;
        private LayoutInflater inflater;
        public CommentAdapter(Context context, int resource, List<CommentModel> objects) {
            super(context, resource, objects);
            commentModelList = objects;
            this.resource = resource;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if(convertView == null)
            {
                convertView = inflater.inflate(resource, null);
            }

            TextView commentUser;
            TextView commentBody;

            commentUser = (TextView)convertView.findViewById(R.id.commentUserNameField);
            commentBody = (TextView)convertView.findViewById(R.id.commentField);

            commentUser.setText(commentModelList.get(position).getUsername() + " : ");
            commentBody.setText(commentModelList.get(position).getCommentsBody());

            return convertView;
        }
    }
}
