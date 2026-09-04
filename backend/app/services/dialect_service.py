# Dialect Intelligence Service
# Maps Language + District → Dialect style for LLM prompt engineering

LANGUAGE_DISTRICTS = {
    "kannada": {
        "Bengaluru": {
            "dialect": "South Kannada (Bengaluru Urban)",
            "style": "modern urban Kannada with English mixing, fast-paced conversational tone",
            "example": "ಇಲ್ಲಿ ನೋಡಿ, ಈ ಕಾನೂನು ಪ್ರಕಾರ..."
        },
        "Mysuru": {
            "dialect": "South Kannada (Mysuru Classical)",
            "style": "classical, formal, respectful Old Mysore Kannada tone",
            "example": "ಶ್ರೀಮಾನರೇ, ಕಾನೂನಿನ ಪ್ರಕಾರ..."
        },
        "Belagavi": {
            "dialect": "North Karnataka Kannada (Belgaum)",
            "style": "North Karnataka dialect with Marathi influence, warm and informal",
            "example": "ಅಲ್ಲಾ ಮಾರಾಯ, ಈ ಕಾನೂನು ಏನಂತದ ಅಂದ್ರ..."
        },
        "Bagalkote": {
            "dialect": "North Karnataka Kannada",
            "style": "North Karnataka dialect, informal and direct",
            "example": "ಮಾರಾಯರ, ಕಾನೂನು ಪ್ರಕಾರ..."
        },
        "Dharwad": {
            "dialect": "North Karnataka Kannada (Dharwad)",
            "style": "Dharwad style Kannada, cultural and slightly formal North Karnataka tone",
            "example": "ನೋಡ್ರಿ, ಈ ಕಾನೂನು..."
        },
        "Kalaburagi": {
            "dialect": "Kalyana Karnataka Kannada (Hyderabad-Karnataka)",
            "style": "Kalyana Karnataka dialect with Urdu/Telugu influence, informal",
            "example": "ಭಾಯಿ, ಕಾನೂನಿಂದ ಏನ್ ಹೇಳ್ತದ..."
        },
        "Bidar": {
            "dialect": "Kalyana Karnataka Kannada",
            "style": "Bidar style Kannada with Urdu influence",
            "example": "ಅರೇ ಭಾಯಿ, ಕಾನೂನು ಪ್ರಕಾರ..."
        },
        "Raichur": {
            "dialect": "Kalyana Karnataka Kannada (Telugu-influenced)",
            "style": "Raichur style with Telugu influence, simple and direct",
            "example": "ಅಣ್ಣ, ಈ ಕಾನೂನು ಏನಂತ..."
        },
        "Ballari": {
            "dialect": "Kalyana Karnataka Kannada",
            "style": "Ballari style Kannada with mining region informality",
            "example": "ನೋಡಣ್ಣ, ಕಾನೂನು ಪ್ರಕಾರ..."
        },
        "Dakshina Kannada": {
            "dialect": "Coastal Kannada (Tulu-influenced)",
            "style": "Coastal Karnataka Kannada with Tulu influence, sing-song tone",
            "example": "ನೋಡ್ರಾ, ಕಾನೂನಿನ ಪ್ರಕಾರ..."
        },
        "Udupi": {
            "dialect": "Coastal Kannada (Udupi)",
            "style": "Udupi coastal style, soft and respectful tone",
            "example": "ನಿಮಗೆ ಹೇಳ್ತೆ, ಕಾನೂನು..."
        },
        "Shivamogga": {
            "dialect": "Malnad Kannada",
            "style": "Malnad/Western Ghats style Kannada, rural and warm",
            "example": "ನೋಡ್ರಿ ಮಾರಾಯ, ಕಾನೂನು..."
        },
        "Hassan": {
            "dialect": "Malnad Kannada",
            "style": "Hassan-region Malnad Kannada, traditional and respectful",
            "example": "ಕಾನೂನಿನ ಪ್ರಕಾರ ಹೇಳ್ಬೇಕು ಅಂದ್ರ..."
        },
        "Vijayapura": {
            "dialect": "North Karnataka Kannada (Bijapur)",
            "style": "Bijapur style with Urdu influence, warm North Karnataka tone",
            "example": "ಅಲ್ಲಾ ಭಾಯಿ, ಕಾನೂನು ಏನ್ ಹೇಳ್ತದ ನೋಡ್ರಿ..."
        },
        "Tumakuru": {
            "dialect": "South Karnataka (Rural)",
            "style": "Tumkur rural South Karnataka Kannada, simple and direct",
            "example": "ಅಣ್ಣ ನೋಡ್ರಿ, ಕಾನೂನು ಪ್ರಕಾರ..."
        },
        "Mandya": {
            "dialect": "South Kannada (Old Mysore, Mandya)",
            "style": "Traditional Mandya Old Mysore Kannada, respectful and formal",
            "example": "ನೋಡ್ರಿ, ಕಾನೂನಿನ ಪ್ರಕಾರ..."
        },
    },
    "marathi": {
        "Nagpur": {
            "dialect": "Vidarbha Marathi (Nagpuri)",
            "style": "Vidarbha dialect, informal Waradi influence, uses 'kay' and 'bav' expressions",
            "example": "बघा बाव, कायद्याप्रमाणे..."
        },
        "Amravati": {
            "dialect": "Vidarbha Marathi",
            "style": "Amravati Vidarbha style, informal and direct",
            "example": "अरे बाबा, हा कायदा सांगतो..."
        },
        "Chh. Sambhajinagar": {
            "dialect": "Marathwada Marathi (Aurangabad)",
            "style": "Marathwada dialect with slight Urdu influence, formal yet warm",
            "example": "भाऊ, कायद्यानुसार पाहा..."
        },
        "Nanded": {
            "dialect": "Marathwada Marathi (Nanded)",
            "style": "Nanded Marathwada style with Urdu touch, informal",
            "example": "बाबा, हे कायदा काय म्हणतो ते बघा..."
        },
        "Latur": {
            "dialect": "Marathwada Marathi (Latur)",
            "style": "Latur style Marathwada Marathi, direct and simple",
            "example": "बघा, कायद्याप्रमाणे..."
        },
        "Pune": {
            "dialect": "Pune Marathi (Standard)",
            "style": "Standard educated Pune Marathi, formal and precise",
            "example": "पाहा, कायद्यानुसार..."
        },
        "Mumbai": {
            "dialect": "Mumbai Marathi (Urban)",
            "style": "Urban fast-paced Mumbai Marathi with Hindi mixing, casual",
            "example": "अरे, हे बघ, कायद्यात काय लिहिलंय..."
        },
        "Nashik": {
            "dialect": "Nashik Marathi",
            "style": "Nashik style Marathi, semi-urban informal",
            "example": "बघा ना, कायद्याप्रमाणे..."
        },
        "Kolhapur": {
            "dialect": "Kolhapuri Marathi",
            "style": "Bold, direct Kolhapuri Marathi, strong informal tone",
            "example": "बघा भाऊ, हा कायदा काय सांगतो..."
        },
        "Ratnagiri": {
            "dialect": "Konkan Marathi",
            "style": "Konkan coastal Marathi, soft sing-song tone",
            "example": "बघा हं, कायद्यानुसार..."
        },
        "Solapur": {
            "dialect": "Western Marathi (Solapur)",
            "style": "Solapur style, semi-formal western Marathi",
            "example": "पाहा, कायद्याप्रमाणे काय आहे ते..."
        },
        "Satara": {
            "dialect": "Western Marathi (Satara)",
            "style": "Satara style, traditional western Marathi",
            "example": "बघा, कायद्यात असं आहे..."
        },
    },
    "hindi": {
        "Lucknow": {
            "dialect": "Awadhi-influenced Lucknawi Hindi",
            "style": "Lucknawi Tehzeeb, extremely polite, uses 'aap', 'janab', 'kripaya', formal and courteous",
            "example": "जनाब, कानून के मुताबिक..."
        },
        "Varanasi": {
            "dialect": "Banarasi Hindi",
            "style": "Banarasi colorful informal Hindi, uses 'ka ho', 'babuaan', lively tone",
            "example": "का हो भाई, ई कानून कहत बा..."
        },
        "Kanpur": {
            "dialect": "Kanpuri Hindi",
            "style": "Kanpur industrial city style, direct and no-nonsense informal",
            "example": "यार, कानून के हिसाब से..."
        },
        "Prayagraj": {
            "dialect": "Awadhi Hindi (Allahabad)",
            "style": "Allahabad style formal Hindi with Awadhi touch, educated tone",
            "example": "देखिए, कानून जो कहता है..."
        },
        "Patna": {
            "dialect": "Bihari Hindi (Bhojpuri/Maithili influenced)",
            "style": "Bihari-influenced Hindi, warm and informal, uses 'ka baat ba'",
            "example": "भाई, कानून का बात ह..."
        },
        "Bhopal": {
            "dialect": "Madhya Pradesh Hindi",
            "style": "Standard MP Hindi, formal and clear",
            "example": "देखो भाई, कानून के अनुसार..."
        },
        "Indore": {
            "dialect": "Malwi-influenced Hindi",
            "style": "Indore urban Malwi-influenced Hindi, casual and friendly",
            "example": "देख यार, कानून में क्या है..."
        },
        "Delhi": {
            "dialect": "Khariboli Standard Hindi",
            "style": "Standard Delhi Hindi, formal and neutral, uses 'aap', 'dekhiye'",
            "example": "देखिए, कानून के अनुसार..."
        },
        "Jaipur": {
            "dialect": "Rajasthani-influenced Hindi",
            "style": "Jaipur Hindi with Dhundhari Rajasthani touch, warm informal",
            "example": "देखो सा, कानून में क्या लिखो है..."
        },
        "Jodhpur": {
            "dialect": "Marwari-influenced Hindi",
            "style": "Jodhpur Marwari-influenced Hindi, business-like informal",
            "example": "भाई सा, कानून का कहणो है..."
        },
        "Gorakhpur": {
            "dialect": "Purvanchali Hindi (Bhojpuri-influenced)",
            "style": "Purvanchali Gorakhpur style, Bhojpuri-influenced informal",
            "example": "भइया, कानून का कहत बा..."
        },
        "Agra": {
            "dialect": "Braj-influenced Hindi",
            "style": "Agra Brajbhasha-influenced Hindi, traditional tone",
            "example": "देखो भाई, कानून क्या कहत है..."
        },
        "Surat": {
            "dialect": "Gujarati-influenced Hindi",
            "style": "Surat Hindi with Gujarati businesslike influence, direct",
            "example": "जुઓ ભઈ, કાਨून ਕੀ ਕਹÀता है..."
        },
        "Dehradun": {
            "dialect": "Pahari-influenced Hindi",
            "style": "Uttarakhand Garhwali-influenced Hindi, respectful mountain style",
            "example": "देखो भाई, कानून के अनुसार..."
        },
    }
}

LANGUAGE_CONFIG = {
    "kannada": {
        "name": "ಕನ್ನಡ",
        "code": "kn",
        "tts_lang": "kn-IN",
        "instruction": "Respond entirely in Kannada script (ಕನ್ನಡ)."
    },
    "marathi": {
        "name": "मराठी",
        "code": "mr",
        "tts_lang": "mr-IN",
        "instruction": "Respond entirely in Marathi script (मराठी)."
    },
    "hindi": {
        "name": "हिंदी",
        "code": "hi",
        "tts_lang": "hi-IN",
        "instruction": "Respond entirely in Hindi (Devanagari script)."
    },
    "english": {
        "name": "English",
        "code": "en",
        "tts_lang": "en-IN",
        "instruction": "Respond entirely in clear, simple English."
    }
}

ALL_STATES_MAP = {
    "Andhra Pradesh": ["Visakhapatnam", "Vijayawada", "Guntur", "Tirupati", "Kakinada", "Nellore", "Kurnool", "Anantapur"],
    "Arunachal Pradesh": ["Itanagar", "Naharlagun", "Tawang"],
    "Assam": ["Guwahati", "Silchar", "Dibrugarh", "Jorhat", "Tezpur"],
    "Bihar": ["Patna", "Gaya", "Muzaffarpur", "Bhagalpur", "Darbhanga", "Purnia", "Arrah"],
    "Chhattisgarh": ["Raipur", "Bhilai", "Bilaspur", "Korba", "Durg"],
    "Delhi": ["Central Delhi", "New Delhi", "North Delhi", "South Delhi", "East Delhi", "West Delhi"],
    "Goa": ["North Goa (Panaji)", "South Goa (Margao)"],
    "Gujarat": ["Ahmedabad", "Surat", "Vadodara", "Rajkot", "Bhavnagar", "Gandhinagar"],
    "Haryana": ["Gurugram", "Faridabad", "Panipat", "Ambala", "Karnal", "Hisar"],
    "Himachal Pradesh": ["Shimla", "Dharamshala", "Mandi", "Solan", "Kullu"],
    "Jharkhand": ["Ranchi", "Jamshedpur", "Dhanbad", "Bokaro", "Hazaribagh"],
    "Karnataka": ["Bengaluru", "Mysuru", "Belagavi", "Bagalkote", "Dharwad", "Kalaburagi", "Bidar", "Raichur", "Ballari", "Dakshina Kannada", "Udupi", "Shivamogga", "Hassan", "Vijayapura", "Tumakuru", "Mandya"],
    "Kerala": ["Thiruvananthapuram", "Kochi", "Kozhikode", "Thrissur", "Kollam", "Kannur"],
    "Madhya Pradesh": ["Bhopal", "Indore", "Gwalior", "Jabalpur", "Ujjain", "Sagar"],
    "Maharashtra": ["Mumbai", "Pune", "Nagpur", "Amravati", "Chh. Sambhajinagar", "Nanded", "Latur", "Nashik", "Kolhapur", "Ratnagiri", "Solapur", "Satara"],
    "Manipur": ["Imphal East", "Imphal West", "Churachandpur"],
    "Meghalaya": ["Shillong", "Tura", "Jowai"],
    "Mizoram": ["Aizawl", "Lunglei"],
    "Nagaland": ["Kohima", "Dimapur"],
    "Odisha": ["Bhubaneswar", "Cuttack", "Rourkela", "Puri", "Sambalpur", "Balasore"],
    "Punjab": ["Ludhiana", "Amritsar", "Jalandhar", "Patiala", "Bathinda", "Mohali"],
    "Rajasthan": ["Jaipur", "Jodhpur", "Udaipur", "Kota", "Ajmer", "Bikaner", "Alwar"],
    "Sikkim": ["Gangtok", "Namchi"],
    "Tamil Nadu": ["Chennai", "Coimbatore", "Madurai", "Tiruchirappalli", "Salem", "Tirunelveli"],
    "Telangana": ["Hyderabad", "Warangal", "Nizamabad", "Karimnagar", "Khammam"],
    "Tripura": ["Agartala", "Udaipur"],
    "Uttar Pradesh": ["Lucknow", "Varanasi", "Kanpur", "Prayagraj", "Gorakhpur", "Agra", "Noida", "Ghaziabad", "Bareilly", "Meerut"],
    "Uttarakhand": ["Dehradun", "Haridwar", "Nainital", "Roorkee", "Haldwani", "Rishikesh"],
    "West Bengal": ["Kolkata", "Howrah", "Durgapur", "Siliguri", "Asansol", "Darjeeling"],
    "All India (National)": ["Central Jurisdiction / Supreme Court", "General / All Districts"]
}


def get_states(language: str = None) -> list:
    """Returns list of all states in India for any selected language."""
    return list(ALL_STATES_MAP.keys())


def get_districts(language: str, state: str = None) -> list:
    """Returns list of districts for a given state."""
    if state and state in ALL_STATES_MAP:
        return ALL_STATES_MAP[state]
    lang = language.lower() if language else ""
    if lang in LANGUAGE_DISTRICTS:
        return list(LANGUAGE_DISTRICTS[lang].keys())
    return ["General / Capital Region"]


def get_dialect_info(language: str, district: str) -> dict:
    """Returns dialect info for a language+district combination."""
    lang = language.lower()
    districts = LANGUAGE_DISTRICTS.get(lang, {})
    return districts.get(district, {
        "dialect": f"Standard {language.capitalize()}",
        "style": f"standard {language} with neutral tone",
        "example": ""
    })


def build_dialect_prompt(language: str, district: str, query: str) -> str:
    """Builds a dialect-aware legal prompt for the LLM."""
    lang = language.lower()

    if lang == "english":
        return f"""You are Vani-Kanoon, an expert AI legal assistant specializing in Indian Law.

Provide a clear, detailed, and easy-to-understand legal answer for the citizen's query.
1. Directly explain their rights and exact legal remedies step-by-step.
2. Mention the specific Indian Law section (e.g. BNS/IPC, BNSS/CrPC, Rent Act, Consumer Protection Act, etc.).
3. Keep the tone warm, clear, and reassuring.

Question: {query}

Clear Legal Answer in English:"""

    lang_config = LANGUAGE_CONFIG.get(lang, {
        "instruction": f"Respond in {language}.",
        "name": language.capitalize(),
        "tts_lang": "hi-IN"
    })
    dialect_info = get_dialect_info(language, district)

    prompt = f"""You are Vani-Kanoon, an expert legal assistant from {district} specializing in Indian Law.

RULES:
- Write ENTIRELY in {language.capitalize()} ({lang_config.get('name', language)}) script.
- Use {dialect_info['dialect']} dialect style spoken in {district}.
- Provide a clear, step-by-step legal answer explaining rights and practical steps.
- Explicitly cite the relevant Indian Law section (e.g. BNS/IPC, BNSS/CrPC, etc.) in {language.capitalize()} script.
- Make the answer clear, complete, helpful, and easy to understand.

Question: {query}

Clear Legal Answer in {dialect_info['dialect']} ({language.capitalize()}):"""

    return prompt

