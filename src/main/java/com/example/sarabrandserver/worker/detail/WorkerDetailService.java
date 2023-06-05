package com.example.sarabrandserver.worker.detail;

import com.example.sarabrandserver.exception.CustomNotFoundException;
import com.example.sarabrandserver.worker.repository.WorkerRepo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service(value = "workerDetailService")
public class WorkerDetailService implements UserDetailsService {

    private final WorkerRepo workerRepo;

    public WorkerDetailService(WorkerRepo workerRepo) {
        this.workerRepo = workerRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.workerRepo.findByPrincipal(username).map(WorkerDetail::new)
                .orElseThrow(() -> new CustomNotFoundException(username + " not found"));
    }
}
