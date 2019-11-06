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
import com.phudnguyen.dusttracker.model.JoinResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class JoinGroupFragment extends Fragment {

    private EditText mGroupCode;
    private EditText mGroupPassword;
    private AsyncTask mAsyncTask;
    private JoinGroupTask joinGroupTask;
    private ActionBar mActionBar;

    public JoinGroupFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mGroupCode = view.findViewById(R.id.group_code);
        mGroupPassword = view.findViewById(R.id.group_password);

        view.findViewById(R.id.btn_create_group).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGroup();
            }
        });
        view.findViewById(R.id.btn_join_group).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                joinGroup();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        mActionBar.setTitle("Join Group");
    }

    private void createGroup() {
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_placeholder, new CreateGroupFragment())
                .addToBackStack(null)
                .commit();
    }

    private void joinGroup() {
        if (mGroupCode.getText() == null || mGroupCode.getText().toString().length() == 0) {
            mGroupCode.setError("Group Code is required");
            return;
        }
        String code = mGroupCode.getText().toString();
        String password = mGroupPassword.getText().toString();
        joinGroupTask = new JoinGroupTask(code, password);
        joinGroupTask.execute();
    }

    public class JoinGroupTask extends AsyncTask<Void, Void, JoinResponse> {

        private final String code;
        private final String password;

        public JoinGroupTask(String code, String password) {
            this.code = code;
            this.password = password;
        }

        @Override
        protected JoinResponse doInBackground(Void... voids) {
            Map<String, String> joinRequest = new HashMap<>();
            joinRequest.put("groupCode", code);
            joinRequest.put("groupPassword", password);
            try {
                return HttpHelper.post(getString(R.string.API_BASE_URL) +
                        getString(R.string.API_JOIN_GROUP), joinRequest, JoinResponse.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JoinResponse joinResponse) {
            super.onPostExecute(joinResponse);
            mGroupPassword.setError(null);
            mGroupCode.setError(null);
            if (joinResponse == null) {
                mGroupCode.setError("Error occur, please try again");
                return;
            }
            if (!joinResponse.isSuccess()) {
                switch (joinResponse.getError()) {
                    case "GROUP_NOT_FOUND":
                        mGroupCode.setError(joinResponse.getError());
                        break;
                    case "INVALID_GROUP_PASSWORD":
                        mGroupPassword.setError(joinResponse.getError());
                        break;
                }
            } else {
                getActivity().getSharedPreferences("appPrefs", Context.MODE_PRIVATE).edit()
                        .putString("currentGroupId", joinResponse.getGroup().getId())
                        .apply();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_placeholder, GroupDetailsFragment.newInstance(joinResponse.getGroup().getId()))
                        .commit();
            }
        }
    }
}
