package com.radiant.account.service;

import com.radiant.account.domain.User;

public interface GddsAccountService extends AccountService {
   User getGddsSystemAdmin();
}
