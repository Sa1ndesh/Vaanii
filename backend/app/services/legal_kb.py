"""
Legal Knowledge Base for RAG
Contains key sections of BNS (Bharatiya Nyaya Sanhita), IPC, CrPC, and common Indian laws.
"""

LEGAL_DOCS = [
    # --- BNS / IPC ---
    {
        "id": "bns_103",
        "title": "Murder (BNS Section 103 / IPC Section 302)",
        "content": "Whoever commits murder shall be punished with death or imprisonment for life and shall also be liable to fine.",
        "keywords": ["murder", "killing", "death", "homicide", "हत्या", "ಕೊಲೆ", "खून"]
    },
    {
        "id": "bns_74",
        "title": "Assault / Hurt (BNS Section 74 / IPC Section 323)",
        "content": "Whoever voluntarily causes hurt to another person shall be punished with imprisonment up to one year, or fine up to ten thousand rupees, or both.",
        "keywords": ["assault", "hurt", "beating", "attack", "fight", "मारपीट", "ಹೊಡೆತ", "मारहाण"]
    },
    {
        "id": "bns_316",
        "title": "Cheating (BNS Section 316 / IPC Section 420)",
        "content": "Whoever cheats and thereby dishonestly induces the person deceived to deliver any property or to make or alter a valuable security, shall be punished with imprisonment up to 7 years and fine.",
        "keywords": ["cheating", "fraud", "deceive", "धोखा", "ठगी", "ಮೋಸ", "फसवणूक"]
    },
    {
        "id": "bns_351",
        "title": "Defamation (BNS Section 356 / IPC Section 499-500)",
        "content": "Whoever, by words, signs or visible representations, makes or publishes any imputation concerning any person intending to harm his reputation, commits defamation. Punishment is simple imprisonment up to 2 years or fine or both.",
        "keywords": ["defamation", "reputation", "slander", "मानहानि", "ಮಾನಹानि", "बदनामी"]
    },
    {
        "id": "bns_85",
        "title": "Cruelty towards Spouse (BNS Section 85 / IPC Section 498A)",
        "content": "Whoever, being the husband or relative of a husband of a woman, subjects such woman to cruelty shall be punished with imprisonment up to 3 years and fine. Cruelty includes harassment for dowry.",
        "keywords": ["dowry", "cruelty", "wife", "498a", "domestic violence", "दहेज", "ಕ್ರೌರ್ಯ", "हुंडा"]
    },
    {
        "id": "pocso_4",
        "title": "Protection of Children (POCSO Act Section 4)",
        "content": "Whoever commits penetrative sexual assault on a child shall be punished with imprisonment not less than 20 years which may extend to imprisonment for life and fine.",
        "keywords": ["child abuse", "pocso", "minor", "sexual assault", "बच्चा", "ಮಕ್ಕಳು"]
    },
    # --- Property ---
    {
        "id": "tp_act_123",
        "title": "Gift Deed (Transfer of Property Act Section 123)",
        "content": "For the purpose of making a gift of immoveable property, the transfer must be effected by a registered instrument signed by or on behalf of the donor, and attested by at least two witnesses.",
        "keywords": ["gift", "donation", "property transfer", "gift deed", "उपहार", "ಉಡುಗೊರೆ"]
    },
    {
        "id": "rent_control",
        "title": "Rent Control and Eviction",
        "content": "A landlord cannot evict a tenant without following proper legal procedure. Notice must be given as per the Rent Control Act. Eviction is allowed for non-payment of rent, damaging property, or subletting without permission.",
        "keywords": ["rent", "eviction", "tenant", "landlord", "house", "किराया", "ಬಾಡಿಗೆ", "भाडे"]
    },
    {
        "id": "rera",
        "title": "RERA - Real Estate (Regulation and Development Act 2016)",
        "content": "RERA protects homebuyers. Builders must register with RERA, cannot take more than 10% as advance, must deliver on time, and are liable for defects for 5 years.",
        "keywords": ["rera", "real estate", "flat", "builder", "apartment", "home", "property", "मकान", "ಮನೆ"]
    },
    # --- Labour ---
    {
        "id": "labour_wages",
        "title": "Minimum Wages Act 1948",
        "content": "Every employer must pay minimum wages as prescribed by the government. Non-payment of minimum wages is an offense punishable with imprisonment up to 6 months or fine up to 500 rupees or both.",
        "keywords": ["wages", "salary", "minimum wage", "payment", "employer", "मजदूरी", "ವೇತನ", "पगार"]
    },
    {
        "id": "labour_pf",
        "title": "Provident Fund (EPF Act 1952)",
        "content": "Employers with 20+ employees must provide EPF (Provident Fund). Both employer and employee contribute 12% of basic salary. The employer cannot withhold the employee's PF.",
        "keywords": ["pf", "provident fund", "epf", "retirement", "job", "employer", "भविष्य निधि", "ಭವಿಷ್ಯ ನಿಧಿ"]
    },
    # --- Consumer ---
    {
        "id": "consumer_protection",
        "title": "Consumer Protection Act 2019",
        "content": "Consumers can file a complaint against defective goods, deficient services, unfair trade practices, and misleading advertisements. District Consumer Forum handles complaints up to Rs. 50 lakhs.",
        "keywords": ["consumer", "complaint", "product", "defect", "service", "refund", "उपभोक्ता", "ಗ್ರಾಹಕ", "ग्राहक"]
    },
    # --- FIR / Police ---
    {
        "id": "fir_crpc_154",
        "title": "FIR - First Information Report (CrPC Section 154 / BNSS Section 173)",
        "content": "Any person can file an FIR for a cognizable offense at any police station. Police cannot refuse to register an FIR. If police refuse, you can directly complain to the Superintendent of Police or file a complaint in a Magistrate's court.",
        "keywords": ["fir", "police", "complaint", "report", "crime", "थाना", "ಠಾಣೆ", "पोलीस"]
    },
    {
        "id": "bail_section",
        "title": "Bail (CrPC Section 436, 437, 439 / BNSS Section 479)",
        "content": "Bail is the temporary release of an accused person while awaiting trial. For bailable offenses, bail is a right. For non-bailable offenses, bail is at the discretion of the court. Anticipatory bail can be obtained before arrest.",
        "keywords": ["bail", "arrest", "custody", "jail", "court", "जमानत", "ಜಾಮೀನು", "जमानीन"]
    },
    # --- Women's Rights ---
    {
        "id": "domestic_violence",
        "title": "Protection of Women from Domestic Violence Act 2005",
        "content": "Women facing domestic violence can file a complaint with a Protection Officer. They have the right to residence in the shared household, protection orders, and monetary relief. The magistrate can pass emergency orders within 3 days.",
        "keywords": ["domestic violence", "wife", "woman", "abuse", "protection", "घरेलू हिंसा", "ಮನೆ ಹಿಂಸೆ", "घरगुती हिंसाचार"]
    },
    {
        "id": "maternity_benefit",
        "title": "Maternity Benefit Act 1961",
        "content": "Women employees are entitled to 26 weeks of paid maternity leave. Employer cannot discharge or dismiss a woman during maternity leave. This applies to establishments with 10 or more employees.",
        "keywords": ["maternity", "pregnancy", "mother", "leave", "baby", "मातृत्व", "ಪ್ರಸೂತಿ", "मातृत्व रजा"]
    },
    # --- Land ---
    {
        "id": "land_acquisition",
        "title": "Land Acquisition Act (RFCTLARR Act 2013)",
        "content": "The government must pay fair compensation (up to 4x market value in rural areas) when acquiring private land. Social Impact Assessment is required. Consent of 70-80% landowners needed for private projects.",
        "keywords": ["land", "acquisition", "compensation", "government", "property", "भूमि", "ಭೂಮಿ", "जमीन"]
    },
]


def search_legal_docs(query: str, top_k: int = 3) -> list:
    """
    Simple keyword-based retrieval from legal knowledge base.
    Returns top_k most relevant legal sections.
    """
    query_lower = query.lower()
    scored = []

    for doc in LEGAL_DOCS:
        score = 0
        # Check title
        if any(word in doc["title"].lower() for word in query_lower.split()):
            score += 3
        # Check content
        if any(word in doc["content"].lower() for word in query_lower.split()):
            score += 2
        # Check keywords
        for kw in doc["keywords"]:
            if kw.lower() in query_lower or query_lower in kw.lower():
                score += 5

        if score > 0:
            scored.append((score, doc))

    scored.sort(key=lambda x: x[0], reverse=True)
    return [doc for _, doc in scored[:top_k]]


def build_rag_context(query: str) -> str:
    """
    Builds a RAG context string from relevant legal documents.
    """
    relevant_docs = search_legal_docs(query)
    if not relevant_docs:
        return ""

    context = "RELEVANT LEGAL SECTIONS FROM KNOWLEDGE BASE:\n"
    for doc in relevant_docs:
        context += f"\n[{doc['title']}]\n{doc['content']}\n"

    return context
