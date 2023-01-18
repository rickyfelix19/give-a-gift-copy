package comp5216.sydney.edu.au.storeapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import comp5216.sydney.edu.au.storeapp.dataHelpers.Staff;

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.ViewHolder> {
    Context context;
    ArrayList<Staff> staffs;
    FirebaseFirestore db;

    public StaffAdapter(Context context, ArrayList<Staff> staffs) {
        this.context = context;
        this.staffs = staffs;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.staff, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Staff staff = staffs.get(position);

        holder.staffName.setText(staff.getName() + " " + staff.getLastName());
        holder.staffEmail.setText(String.valueOf(staff.getEmail()));

        if (staff.getProfileImage() != null) {
            StorageReference storageReference =
                    FirebaseStorage.getInstance().getReference(staff.getProfileImage());
            // Load the image using Glide
            Glide.with(context).load(storageReference).into(holder.staffImage);;
        }

        holder.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
                builder.setTitle("Remove a Staff member");
                builder.setMessage("Do you want to remove this staff?");
                builder.setPositiveButton("Yes", (dialogInterface, i) -> {
                    db.collection("userStores").document(staff.getUserStoresId()).delete();
                    staffs.remove(position);
                    notifyDataSetChanged();
                });
                builder.setNegativeButton("No", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                });
                builder.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return staffs.size();
    }

    public void filterList(ArrayList<Staff> filteredList) {
        staffs = filteredList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView staffName, staffEmail;
        public ImageView staffImage;
        public FloatingActionButton btn;

        public ViewHolder(View itemView) {
            super(itemView);
            staffName = (TextView) itemView.findViewById(R.id.staffName);
            staffEmail = (TextView) itemView.findViewById(R.id.staffEmail);
            staffImage = (ImageView) itemView.findViewById(R.id.staffImage);
            btn = (FloatingActionButton) itemView.findViewById(R.id.removeStaff);
        }
    }
}

