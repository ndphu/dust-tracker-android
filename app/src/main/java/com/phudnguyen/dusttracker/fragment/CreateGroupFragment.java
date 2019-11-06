package com.phudnguyen.dusttracker.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.phudnguyen.dusttracker.R;
import com.phudnguyen.dusttracker.http.HttpHelper;
import com.phudnguyen.dusttracker.model.CreateGroupResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class CreateGroupFragment extends Fragment {

    private EditText mGroupPassword;
    private EditText mGroupName;
    CreateGroupTask task;

    public CreateGroupFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_group, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mGroupName = view.findViewById(R.id.group_name);
        mGroupPassword = view.findViewById(R.id.group_password);

        view.findViewById(R.id.btn_create_group).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGroup();
            }
        });
    }

    private void createGroup() {
        if (task != null) {
            return;
        }

        if (mGroupName.getText() == null || mGroupName.getText().toString().trim().length() == 0) {
            mGroupName.setError("Group Name is required");
            return;
        }

        task = new CreateGroupTask(mGroupName.getText().toString(), mGroupPassword.getText().toString());
        task.execute();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle("Create Group");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public class CreateGroupTask extends AsyncTask<Void, Void, CreateGroupResponse> {

        private String groupName;
        private String groupPassword;

        public CreateGroupTask(String groupName, String groupPassword) {
            this.groupName = groupName;
            this.groupPassword = groupPassword;
        }

        @Override
        protected CreateGroupResponse doInBackground(Void... voids) {
            Map<String, String> createGroupRequest = new HashMap<>();
            createGroupRequest.put("name", groupName);
            createGroupRequest.put("password", groupPassword);
            try {
                return HttpHelper.post(getString(R.string.API_BASE_URL) +
                        getString(R.string.API_CREATE_GROUP), createGroupRequest, CreateGroupResponse.class);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(CreateGroupResponse createGroupResponse) {
            super.onPostExecute(createGroupResponse);
            if (createGroupResponse == null) {
                mGroupName.setError("Error occur, please try again");
                return;
            }

            if (!createGroupResponse.isSuccess()) {
                mGroupName.setError(createGroupResponse.getError());
                return;
            }
            mGroupName.setError(null);

            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_placeholder, GroupDetailsFragment.newInstance(createGroupResponse.getGroup().getId()))
                    .commit();
        }
    }
}