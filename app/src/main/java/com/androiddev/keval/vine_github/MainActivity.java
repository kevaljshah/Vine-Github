package com.androiddev.keval.vine_github;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

public class MainActivity extends AppCompatActivity {

    private ListView issueList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        issueList = (ListView)findViewById(R.id.listIssues);
        
        new JSONParse().execute("https://api.github.com/repos/rails/rails/issues?sort=updated");


    }



    public class JSONParse extends AsyncTask<String, String, List<IssueModel> >
    {

        @Override
        protected List<IssueModel> doInBackground(String... params)
        {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try
            {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line ="";
                while((line=reader.readLine()) != null)
                {
                    buffer.append(line);
                }
                String JSONread = buffer.toString();

                JSONArray json = new JSONArray(JSONread);

                List<IssueModel> issueModelList = new ArrayList<>();

                for(int i = 0; i<json.length();i++)
                {
                    JSONObject issueObject = json.getJSONObject(i);
                    IssueModel issueModel = new IssueModel();


                    issueModel.setTitle(issueObject.getString("title"));
                    issueModel.setBody(issueObject.getString("body"));
                    issueModel.setComments(issueObject.getString("comments_url"));
                    issueModelList.add(issueModel);
                }

                return issueModelList;

            }
            catch(MalformedURLException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            finally
            {
                if(connection != null)
                {
                    connection.disconnect();
                }
                try
                {
                    if(reader != null)
                    {
                        reader.close();
                    }
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<IssueModel> result) {
            super.onPostExecute(result);

            IssueAdapter adapter = new IssueAdapter(getApplicationContext(), R.layout.list_view_layout, result);
            issueList.setAdapter(adapter);

            issueList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    IssueModel m = (IssueModel) issueList.getItemAtPosition(position);
                    Intent i = new Intent(MainActivity.this, IssueCommentActivity.class);
                    i.putExtra("commentsurl", m.getComments());

                    startActivity(i);
                }
            });
        }
    }

    public class IssueAdapter extends ArrayAdapter
    {
        private List<IssueModel> issueModelList;
        private int resource;
        private LayoutInflater inflater;
        public IssueAdapter(Context context, int resource, List<IssueModel> objects) {
            super(context, resource, objects);
            issueModelList = objects;
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

            TextView issueTitle;
            TextView issueBody;
            //TextView issueComment;

            issueTitle = (TextView)convertView.findViewById(R.id.issueTitleField);
            issueBody = (TextView)convertView.findViewById(R.id.issueBodyField);
            //issueComment = (TextView)convertView.findViewById(R.id.issueCommentField);

            issueTitle.setText(issueModelList.get(position).getTitle());
            issueBody.setText(issueModelList.get(position).getBody());
            //issueComment.setText(issueModelList.get(position).getComments());

            return convertView;
        }
    }
}
