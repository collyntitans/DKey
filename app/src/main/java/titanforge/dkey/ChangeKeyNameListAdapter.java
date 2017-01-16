package titanforge.dkey;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nicky Fandino on 12/6/2016.
 */

public class ChangeKeyNameListAdapter extends RecyclerView.Adapter<ChangeKeyNameListAdapter.MyViewHolder> {

    private List<ChangeKeyNameListView> keyList;
    private OnTapListener onTapListener = null;
    private List<MyViewHolder> viewHolderList = new ArrayList<>(1);

    public int lastTouchPosition = -1;

    public interface OnTapListener{
        public void onTap(String keyName, long keyID);
    }

    public void setOnTapListener(OnTapListener onTapListener){
        this.onTapListener = onTapListener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView keyName;
        public long keyID;
        public int position;
        private int currentColor;
        private LinearLayout linearLayout;

        public MyViewHolder(View view) {
            super(view);
            keyName = (TextView) view.findViewById(R.id.change_door_name_text);
            linearLayout = (LinearLayout) view.findViewById(R.id.list_change_keys_layout);
            linearLayout.setOnClickListener(this);
            currentColor = Color.WHITE;
            linearLayout.setBackgroundColor(currentColor);
        }

        @Override
        public void onClick(View view) {
            if(view == linearLayout){
                if(lastTouchPosition >= 0) {
                    viewHolderList.get(lastTouchPosition).toggleBackground();
                }
                lastTouchPosition = position;
                if(onTapListener != null) {
                    onTapListener.onTap(keyName.getText().toString(), keyID);
                }
                toggleBackground();
            }
        }

        public void toggleBackground(){
            if(currentColor == Color.WHITE){
                currentColor = Color.GRAY;
            }else{
                currentColor = Color.WHITE;
            }
            linearLayout.setBackgroundColor(currentColor);
        }


    }


    public ChangeKeyNameListAdapter(List<ChangeKeyNameListView> keyList) {
        this.keyList = keyList;
    }

    @Override
    public ChangeKeyNameListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_change_key_name, parent, false);

        return new ChangeKeyNameListAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ChangeKeyNameListAdapter.MyViewHolder holder, int position) {
        ChangeKeyNameListView changeKeyNameListView = keyList.get(position);
        holder.keyName.setText(changeKeyNameListView.getKeyName());
        holder.keyID = changeKeyNameListView.getKeyID();
        viewHolderList.add(holder);
        holder.position = viewHolderList.indexOf(holder);
    }

    @Override
    public int getItemCount() {
        return keyList.size();
    }
}