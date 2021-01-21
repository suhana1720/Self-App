package com.gohool.self.ui;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gohool.self.R;
import com.gohool.self.model.Journal;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>  {
    private Context context;
    private List<Journal> JournalList;
    public RecyclerViewAdapter(Context context, List<Journal> JournalList) {
        this.context=context;
        this.JournalList=JournalList;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {// here we get view
        View view= LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.journal_row, viewGroup,false);

        return new ViewHolder(view, context); // creating vh obj

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewholder, int position) {

        Journal journal = JournalList.get(position); // getting 1 item fromjl
        String imageUrl;
// constructing view with data
        viewholder.title.setText(journal.getTitle());
        viewholder.thoughts.setText(journal.getThought());
        viewholder.name.setText(journal.getUserName());
        imageUrl = journal.getImageUrl();
        //1 hour ago..(like this)
        //Source: https://medium.com/@shaktisinh/time-a-go-in-android-8bad8b171f87
        String timeAgo = (String) DateUtils.getRelativeTimeSpanString(journal
                .getTimeAdded()
                .getSeconds() * 1000);
        viewholder.dateAdded.setText(timeAgo);


        /*
         Use Picasso library to download and show image
         */
        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.image_three)
                .fit()
                .into(viewholder.image);

    }


    @Override
    public int getItemCount() {
        return JournalList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {// this class contain all of widges

        public TextView
                title,
                thoughts,
                dateAdded,
                name;
        public ImageView image;
        public ImageButton share;
        String userId;
        String username;

        public ViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            context=context;
            title = itemView.findViewById(R.id.journal_title_list);
            thoughts = itemView.findViewById(R.id.journal_thought_list);
            dateAdded = itemView.findViewById(R.id.journal_timestand_list);
            image = itemView.findViewById(R.id.journal_image_list);
           name = itemView.findViewById(R.id.journal_row_username);
           share=itemView.findViewById(R.id.journal_row_share_button);

        }
    }
}
