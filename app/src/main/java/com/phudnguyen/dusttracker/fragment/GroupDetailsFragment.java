package com.phudnguyen.dusttracker.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.phudnguyen.dusttracker.R;
import com.phudnguyen.dusttracker.http.HttpHelper;
import com.phudnguyen.dusttracker.model.Group;
import com.phudnguyen.dusttracker.model.GroupDetailsResponse;


public class GroupDetailsFragment extends Fragment {
    private static final String ARG_GROUP_ID = "group_id";

    private String groupId;
    private LoadGroupTask loadGroupTask;
    private TextView groupName;
    private TextView groupPassword;
    private TextView groupCode;
    private ActionBar mActionBar;

    public GroupDetailsFragment() {

    }

    public static GroupDetailsFragment newInstance(String groupId) {
        GroupDetailsFragment fragment = new GroupDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID, groupId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            groupId = getArguments().getString(ARG_GROUP_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_details, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        groupName = view.findViewById(R.id.group_name);
        groupPassword = view.findViewById(R.id.group_password);
        groupCode = view.findViewById(R.id.group_code);

        loadGroup();

    }

    private void loadGroup() {
        if (this.loadGroupTask != null) return;
        this.loadGroupTask = new LoadGroupTask(this.groupId);
        this.loadGroupTask.execute();
    }

    public class LoadGroupTask extends AsyncTask<Void, Void, GroupDetailsResponse> {

        private final String groupId;

        public LoadGroupTask(String groupId) {
            this.groupId = groupId;
        }

        @Override
        protected GroupDetailsResponse doInBackground(Void... voids) {
            try {
                return HttpHelper.get(getString(R.string.API_BASE_URL) +
                        getString(R.string.API_RETRIEVE_GROUP) + this.groupId, GroupDetailsResponse.class);
            } catch (Exception ex) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(GroupDetailsResponse groupDetailsResponse) {
            super.onPostExecute(groupDetailsResponse);
            if (groupDetailsResponse.isSuccess()) {
                showGroupDetails(groupDetailsResponse.getGroup());
            }
        }
    }

    private void showGroupDetails(Group group) {
        this.groupCode.setText(group.getCode());
        if (mActionBar != null) {
            mActionBar.setTitle(group.getName());
        }
    }
}
