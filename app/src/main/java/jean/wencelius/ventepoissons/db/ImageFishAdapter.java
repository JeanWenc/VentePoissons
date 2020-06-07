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
    private ArrayList<ImageUrl> imageUrls;
    private Context context;
    private ImageFishAdapter.OnImageListener mOnImageListener;
    private String[] fishCount;

    public ImageFishAdapter(Context context, ArrayList<ImageUrl> imageUrls, String[] fishCount,ImageFishAdapter.OnImageListener onImageListener) {
        this.context = context;
        this.mOnImageListener = onImageListener;
        this.imageUrls = imageUrls;
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

        String imagePath = imageUrls.get(i).getImageUrl();
        Uri imageUri = null;
        if(!imagePath.equals("android.resource://jean.wencelius.traceurrecopem/drawable/add_image")){
            File file = new File(imagePath);
            imageUri = Uri.fromFile(file);
        }else{
            imageUri = Uri.parse(imagePath);
        }

        Glide.with(context).load(imageUri).into(viewHolder.img);
        if(fishCount[i]!=null){
            viewHolder.text.setText(fishCount[i]);
        }else{
            viewHolder.text.setText("0");
        }
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView img;
        TextView text;
        ImageFishAdapter.OnImageListener onImageListener;

        public ViewHolder(View view, ImageFishAdapter.OnImageListener onImageListener) {
            super(view);
            img = view.findViewById(R.id.image_fish_item_id);
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

