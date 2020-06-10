package jean.wencelius.ventepoissons.db;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

import jean.wencelius.ventepoissons.R;
import jean.wencelius.ventepoissons.model.ImageUrl;

/**
 * Created by Jean Wencélius on 12/05/2020.
 */
public class ImageFishAdapter  extends RecyclerView.Adapter<ImageFishAdapter.ViewHolder> {
    private String [] fishNames;
    private Context context;
    private ImageFishAdapter.OnImageListener mOnImageListener;
    private String[] fishCount;

    public ImageFishAdapter(Context context, String[] fishNames, String[] fishCount,ImageFishAdapter.OnImageListener onImageListener) {
        this.context = context;
        this.mOnImageListener = onImageListener;
        this.fishNames = fishNames;
        this.fishCount = fishCount;
    }

    @Override
    public ImageFishAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.image_fish_item, viewGroup, false);
        return new ImageFishAdapter.ViewHolder(view, mOnImageListener);
    }

    /**
     * gets the image url from adapter and passes to Glide API to load the image
     *
     * @param viewHolder
     * @param i
     */
    @Override
    public void onBindViewHolder(ImageFishAdapter.ViewHolder viewHolder, int i) {

        viewHolder.name.setText(fishNames[i]);

        if(fishCount[i]!=null){
            viewHolder.text.setText(fishCount[i]);
        }else{
            viewHolder.text.setText("0");
        }
    }

    @Override
    public int getItemCount() {
        return fishNames.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView name;
        TextView text;
        ImageFishAdapter.OnImageListener onImageListener;

        public ViewHolder(View view, ImageFishAdapter.OnImageListener onImageListener) {
            super(view);
            name = view.findViewById(R.id.image_fish_item_name);
            text = view.findViewById(R.id.image_fish_item_tv);

            this.onImageListener = onImageListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onImageListener.onImageClick(getAdapterPosition());
        }
    }

    public interface OnImageListener{
        void onImageClick(int position);
    }
}
