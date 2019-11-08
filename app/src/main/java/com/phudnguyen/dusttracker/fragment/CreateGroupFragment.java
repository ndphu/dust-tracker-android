package com.phudnguyen.dusttracker.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.phudnguyen.dusttracker.R;
import com.phudnguyen.dusttracker.http.HttpHelper;
import com.phudnguyen.dusttracker.model.CreateGroupResponse;
import com.phudnguyen.dusttracker.model.Group;
import com.phudnguyen.dusttracker.utils.AppPrefs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class CreateGroupFragment extends Fragment {

    private EditText mGroupPassword;
    private EditText mGroupName;
    private CreateGroupTask mCreateGroupTask;
    private CreateGroupFragmentListener mListener;
    private ProgressBar mCreateGroupProgress;
    private View mCreateGroupForm;

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
        mCreateGroupForm = view.findViewById(R.id.create_group_form);
        mCreateGroupProgress = view.findViewById(R.id.create_group_progress);

        view.findViewById(R.id.btn_create_group).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGroup();
            }
        });
    }

    private void createGroup() {
        if (mCreateGroupTask != null) {
            return;
        }

        if (mGroupName.getText() == null || mGroupName.getText().toString().trim().length() == 0) {
            mGroupName.setError("Group Name is required");
            return;
        }

        mCreateGroupTask = new CreateGroupTask(mGroupName.getText().toString(), mGroupPassword.getText().toString());
        mCreateGroupTask.execute();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Create Group");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        mListener = (CreateGroupFragmentListener) getActivity();
    }

    @Override
    public void onDetach() {
        mListener = null;
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
        protected void onPreExecute() {
            super.onPreExecute();
            mCreateGroupProgress.setVisibility(View.VISIBLE);
            mCreateGroupForm.setVisibility(View.GONE);
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
            Group group = createGroupResponse.getGroup();
            AppPrefs.setGroupId(getActivity(), group.getId());
            if (mListener != null) {
                mListener.onCreateGroupSuccess(group);
            }
            mCreateGroupProgress.setVisibility(View.GONE);
            mCreateGroupForm.setVisibility(View.VISIBLE);
        }
    }

    public interface CreateGroupFragmentListener {
        void onCreateGroupSuccess(Group group);
    }
}