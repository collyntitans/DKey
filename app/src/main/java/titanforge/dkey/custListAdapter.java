package titanforge.dkey;

/**
 * Created by nicky on 10/28/16.
 */

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class custListAdapter extends RecyclerView.Adapter<custListAdapter.MyViewHolder> {

    private List<CustListContent> doorList;
    private onActionClickListener actionClickListener = null;

    public interface onActionClickListener{
        public void onAction(String doorName,boolean isCheckIn, String deviceAddress);
        public void onUnlock(String doorName, String deviceAddress);
        public void onLock(String doorName, String deviceAddress);
    }

    public void setOnActionClickListener(onActionClickListener actionClickListener){
        this.actionClickListener = actionClickListener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView doorName;
        public TextView doorStatus;
        public TextView doorDesc;
        public Button actionBtn;
        public Button unlockBtn;
        public Button lockBtn;

        public boolean isCheckIn;
        public String deviceAddress;

        public MyViewHolder(View view) {
            super(view);
            doorDesc = (TextView)view.findViewById(R.id.door_description);
            doorStatus = (TextView)view.findViewById(R.id.door_status);
            doorName = (TextView)view.findViewById(R.id.door_name);
            actionBtn = (Button)view.findViewById(R.id.actionBtn);
            unlockBtn = (Button)view.findViewById(R.id.unlockBtn);
            lockBtn = (Button)view.findViewById(R.id.lockBtn);

            actionBtn.setOnClickListener(this);
            unlockBtn.setOnClickListener(this);
            lockBtn.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(view == actionBtn){
                actionClickListener.onAction(doorName.getText().toString(), isCheckIn, deviceAddress);
            }if(view == lockBtn){
                actionClickListener.onLock(doorName.getText().toString(), deviceAddress);
            }if(view == unlockBtn){
                actionClickListener.onUnlock(doorName.getText().toString(), deviceAddress);
            }
        }
    }


    public custListAdapter(List<CustListContent> doorList) {
        this.doorList = doorList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_room, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        CustListContent custListContent = doorList.get(position);
        holder.doorName.setText(custListContent.getUnlockDoorName());
        holder.doorStatus.setText(custListContent.getStatus());
        holder.doorDesc.setText(custListContent.getDesc());
        if(custListContent.getCheckIn()){
            holder.actionBtn.setText("Open");
            holder.unlockBtn.setVisibility(View.VISIBLE);
            holder.lockBtn.setVisibility(View.VISIBLE);
        }else{
            holder.actionBtn.setText("Check-In");
            holder.unlockBtn.setVisibility(View.INVISIBLE);
            holder.lockBtn.setVisibility(View.INVISIBLE);
        }
        holder.isCheckIn = custListContent.getCheckIn();
        holder.deviceAddress = custListContent.getDeviceAddress();
    }

    @Override
    public int getItemCount() {
        return doorList.size();
    }
}