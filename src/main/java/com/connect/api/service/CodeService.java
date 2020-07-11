package com.connect.api.service;

import com.connect.api.dto.entity.Code;
import com.connect.api.repository.CodeRepository;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class CodeService {

    private final CodeRepository codeRepository;

    public CodeService(CodeRepository codeRepository) {
        this.codeRepository = codeRepository;
    }

    public String createCode(Long userId) {
        String alphanumericCode = createAlphanumericString();

        Code code = new Code();
        code.setCode(alphanumericCode);
        code.setUserId(userId);
        codeRepository.save(code);

        return alphanumericCode;
    }

    private String createAlphanumericString() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public Long isCodeValid(String code) {

        if (code == null) {
            return null;
        }

        Long codeId = codeRepository.getCode(code);
        if (codeId == null) {
            return null;
        }

        Code codeEntity = codeRepository.findById(codeId).orElse(null);

        if (codeEntity == null) {
            return null;
        }

        return codeEntity.getUserId();
    }

    public void removeCode(String code) {
        Long codeId = codeRepository.getCode(code);
        if (codeId == null) {
            return;
        }

        Code codeEntity = codeRepository.findById(codeId).orElse(null);
        if (codeEntity == null) {
            return;
        }

        codeRepository.delete(codeEntity);
    }
}
