package com.example.myapplication2;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class PlaceInfoDialog extends AppCompatActivity {

    private PlaceSelectedListener placeSelectedListener;
    private GooglePlace place;
    private Context context;
    private Dialog dialog;
    private View view;
    private ListView listView;
    private TextView NameOfStation;

    private TextView Address;
    private String address;

    private TextView Operational;
    private String operational;

    private TextView Website;
    private String website;
    private Button buttonDismiss;
    private Button moreInfoButton;
    private ImageView placePhoto;
    private String photoUrl;
    private String isOpen;
    private TextView isOpenTextView;
    private ImageView WebsiteImage;
    private ImageView PriceImage;


    public PlaceInfoDialog(Context context, GooglePlace place, String address, String operational, String website, String photoUrl, String isOpen, PlaceSelectedListener placeSelectedListener) {

        this.context = context;
        this.place = place;
        this.placeSelectedListener = placeSelectedListener;
        this.address = address;
        this.operational = operational;
        this.website = website;
        this.photoUrl = photoUrl;
        this.isOpen = isOpen;


        setupDialog();
        setupUI();
        //setupListView();
        setButtonClickListener();




    }

    public void showDialog(){
        dialog.show();
    }

    private void setupDialog(){
        dialog = new Dialog(context);
        view = LayoutInflater.from(context).inflate(R.layout.list_view_dialog, null);
        dialog.setContentView(view);
    }

    private void setupUI(){
        //listView = view.findViewById(R.id.listView);
        NameOfStation = (TextView) view.findViewById(R.id.NameOfStation);
        Address = (TextView) view.findViewById(R.id.Address);
        //Operational = (TextView) view.findViewById(R.id.Operational);
        Website = (TextView) view.findViewById(R.id.Website);
        placePhoto = (ImageView) view.findViewById(R.id.photo);
        WebsiteImage = view.findViewById(R.id.website2);
        PriceImage = view.findViewById(R.id.price2);
        //isOpenTextView = (TextView) view.findViewById(R.id.IsOpen);

        NameOfStation.setText(this.place.getName());
        Address.setText(this.address);
        //Operational.setText(this.operational);
        Website.setText(this.website);
        //int imageResource = getResources().getIdentifier("@drawable/website", null, this.getPackageName());
        WebsiteImage.setImageResource(R.drawable.website);
        PriceImage.setImageResource(R.drawable.price);

        //isOpenTextView.setText(isOpen);

        if (photoUrl == null) {
//            System.out.println("PHOTO URL IS EMPTY");
            placePhoto.setImageResource(R.drawable.common_google_signin_btn_icon_dark);
        } else {
            Picasso.get()
                    .load(photoUrl)
                    //This is how to limit the size of the pictures
                    .resize(140, 150)
                    .into(placePhoto);
        }

        buttonDismiss = view.findViewById(R.id.buttonDismiss);
        moreInfoButton = view.findViewById(R.id.MoreInfoButton);
    }



    private void setButtonClickListener() {
        buttonDismiss.setOnClickListener(e -> dialog.dismiss());
        moreInfoButton.setOnClickListener(e -> {
            GooglePlace googlePlace = this.place;
            placeSelectedListener.getPlace(googlePlace);
        });
    }


}