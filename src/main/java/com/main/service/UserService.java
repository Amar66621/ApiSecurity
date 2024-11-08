package com.main.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.main.entity.UserEntity;

@Service
public class UserService {
	
	private List<UserEntity> store = new ArrayList<UserEntity>();
	
	public UserService() {
		store.add(new UserEntity(UUID.randomUUID().toString(), "AmarJeet", "aj42345@gamil.com", "Amarjeet"));
		store.add(new UserEntity(UUID.randomUUID().toString(), "Amar", "Amart@gamil.com", "jeet"));
		store.add(new UserEntity(UUID.randomUUID().toString(), "Singh", "Singh@gamil.com", "Amar"));
		
	}

	public List<UserEntity> getuser(){
		return this.store;
	}
}
