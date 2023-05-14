package com.example.bluetoothapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bluetoothapp.Models.Message;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder>{

    Context context;
    List<Message> messageList;

    public MessageAdapter(Context context, List<Message> messages){
        this.context = context;
        this.messageList = messages;
    }
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MessageViewHolder(LayoutInflater.from(context).inflate(R.layout.message_cardview, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (message.getSender().equals("me")){
            holder.messageCardView.setBackgroundColor(context.getResources().getColor(R.color.orange));
        }

        holder.senderName.setText(message.getSender());
        holder.message.setText(message.getMessage());
        holder.date.setText(message.getCreatedAt().toString());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void addMessage(Message newMessage){
        int position = messageList.size();
        messageList.add(newMessage);
        notifyDataSetChanged();
    }
}
class MessageViewHolder extends RecyclerView.ViewHolder{
    CardView messageCardView;

    TextView senderName, message, date;

    public MessageViewHolder(@NonNull View itemView) {
        super(itemView);

        messageCardView = itemView.findViewById(R.id.messageCardView);
        senderName = itemView.findViewById(R.id.senderName);
        message = itemView.findViewById(R.id.messageContent);
        date = itemView.findViewById(R.id.createdAt);
    }

}