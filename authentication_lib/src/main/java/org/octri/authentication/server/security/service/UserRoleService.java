package org.octri.authentication.server.security.service;

import java.util.List;

import org.octri.authentication.server.security.entity.UserRole;
import org.octri.authentication.server.security.repository.UserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

/**
 * Service wrapper for the {@link UserRoleRepository}.
 *
 * @author sams
 */
@Service
public class UserRoleService {

	@Resource
	private UserRoleRepository userRoleRepository;

	@Transactional(readOnly = true)
	public UserRole find(Long id) {
		return userRoleRepository.findById(id).get();
	}

	@Transactional(readOnly = true)
	public UserRole findByRoleName(String roleName) {
		return userRoleRepository.findByRoleName(roleName);
	}

	@Transactional
	public UserRole save(UserRole userRole) {
		return userRoleRepository.save(userRole);
	}

	@Transactional(readOnly = true)
	public List<UserRole> findAll() {
		return (List<UserRole>) userRoleRepository.findAll();
	}

	@Transactional
	public void delete(Long id) {
		UserRole userRole = userRoleRepository.findById(id).orElse(null);
		if (userRole != null) {
			userRoleRepository.deleteById(id);
		}
	}

}
