package titanforge.dkey;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by nicky on 11/15/16.
 */

public class ListOfKeysAdapter extends RecyclerView.Adapter<ListOfKeysAdapter.MyViewHolder> {

    private List<ListOfKeysList> keyList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView roomName, availableFrom, availableUntil, dateRedeemed;

        public MyViewHolder(View view) {
            super(view);
            roomName = (TextView) view.findViewById(R.id.text_room_name);
            availableFrom = (TextView) view.findViewById(R.id.text_available_from);
            availableUntil = (TextView) view.findViewById(R.id.text_available_until);
            dateRedeemed = (TextView) view.findViewById(R.id.text_date_redeemed);
        }
    }


    public ListOfKeysAdapter(List<ListOfKeysList> keyList) {
        this.keyList = keyList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_owned_keys, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ListOfKeysList listOfKeys = keyList.get(position);
        holder.roomName.setText(listOfKeys.getRoomName());
        holder.availableFrom.setText(listOfKeys.getAvailableFrom());
        holder.availableUntil.setText(listOfKeys.getAvailableUntil());
        holder.dateRedeemed.setText(listOfKeys.getDateRedeemed());
    }

    @Override
    public int getItemCount() {
        return keyList.size();
    }
}