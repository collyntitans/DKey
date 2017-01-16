package titanforge.dkey;

import android.view.View;

/**
 * Created by nicky on 11/7/16.
 */
interface ClickListener {
    void onClick(View view, int position);

    void onLongClick(View view, int position);
}
