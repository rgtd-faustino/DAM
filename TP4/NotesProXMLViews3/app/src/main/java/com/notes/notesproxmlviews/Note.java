package com.notes.notesproxmlviews;

import com.google.firebase.Timestamp;

public class Note {
    String title;
    String content;
    Timestamp timestamp;

    public Note() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    // guardamos apenas o URL da imagem e não a imagem em si no firestore,
    // porque o firestore é uma base de dados de texto e não suporta ficheiros pesados,
    // o ficheiro em si fica no firebase storage e aqui ficamos só com o endereço para o ir buscar
    String imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // data a partir da qual a nota fica acessível, null significa que não está bloqueada
    Timestamp unlockDate;

    public Timestamp getUnlockDate() {
        return unlockDate;
    }

    public void setUnlockDate(Timestamp unlockDate) {
        this.unlockDate = unlockDate;
    }
}

