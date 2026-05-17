package com.example.beautysalonreview.util;

import com.example.beautysalonreview.model.*;
import com.example.beautysalonreview.controller.*;
import com.example.beautysalonreview.repository.*;
import com.example.beautysalonreview.util.*;




import jakarta.servlet.http.HttpSession;

public class SecurityUtils {
    public static boolean isManager(HttpSession session) {
        return "MANAGER".equals(session.getAttribute("staffRole"));
    }
}
