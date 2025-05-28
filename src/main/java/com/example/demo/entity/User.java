package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name="tbl_userss")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "user_seq", allocationSize = 1)
    private int id;
    private String nama;
    private String email;

    public int getId() {
    	return id; 
    }
    public void setId(int id) {
    	this.id = id; 	
    }
    
    public String getNama() { 
    	return nama; 
    }
    
    public void setNama(String nama) {
    	this.nama = nama; 	
    }
    public String getEmail() {
    	return email; 
    }
    public void setEmail(String email) {
    	this.email = email; 
    }
}
