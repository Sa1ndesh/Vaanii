/**
 * Offline Legal Knowledge Base
 * Pre-computed answers for common Indian legal questions
 * Works 100% offline - no internet needed
 */

// BNS 2023 Key Sections (replaced IPC)
const BNS_SECTIONS = {
  "103": { title: "Murder", oldIPC: "302", punishment: "Death or life imprisonment + fine", description: "Whoever causes death by doing an act with intention or knowledge that death will occur" },
  "105": { title: "Culpable Homicide", oldIPC: "304", punishment: "Life imprisonment or up to 10 years + fine", description: "Causing death without premeditation in sudden fight or heat of passion" },
  "115": { title: "Voluntarily causing hurt", oldIPC: "323", punishment: "Up to 1 year or fine up to ₹10,000 or both", description: "Causing bodily pain, disease or infirmity" },
  "117": { title: "Grievous hurt", oldIPC: "325", punishment: "Up to 7 years + fine", description: "Causing permanent disfigurement, bone fracture, or danger to life" },
  "63": { title: "Rape", oldIPC: "376", punishment: "Minimum 10 years to life imprisonment + fine", description: "Sexual intercourse without consent" },
  "74": { title: "Assault on woman with intent to outrage modesty", oldIPC: "354", punishment: "1-5 years + fine", description: "Using criminal force on woman to outrage her modesty" },
  "303": { title: "Theft", oldIPC: "379", punishment: "Up to 3 years or fine or both", description: "Dishonestly taking movable property without consent" },
  "309": { title: "Robbery", oldIPC: "392", punishment: "Up to 10 years + fine", description: "Theft with violence or threat of violence" },
  "310": { title: "Dacoity", oldIPC: "395", punishment: "Up to 10 years + fine, if with murder - death/life", description: "Robbery by 5 or more persons" },
  "318": { title: "Cheating", oldIPC: "420", punishment: "Up to 7 years + fine", description: "Deceiving and dishonestly inducing delivery of property" },
  "329": { title: "Criminal breach of trust", oldIPC: "406", punishment: "Up to 3 years or fine or both", description: "Dishonest misappropriation of entrusted property" },
  "351": { title: "Criminal intimidation", oldIPC: "506", punishment: "Up to 2 years or fine or both", description: "Threatening with injury to person, reputation or property" },
  "356": { title: "Defamation", oldIPC: "499/500", punishment: "Up to 2 years or fine or both", description: "Making statements that harm reputation" },
  "85": { title: "Dowry death", oldIPC: "304B", punishment: "Minimum 7 years to life imprisonment", description: "Death of woman within 7 years of marriage due to cruelty for dowry" },
  "86": { title: "Cruelty by husband/relatives", oldIPC: "498A", punishment: "Up to 3 years + fine", description: "Cruelty towards married woman by husband or his relatives" },
};

// Common Legal Q&As Database
const LEGAL_QA = [
  // FIR Related
  {
    keywords: ["fir", "file", "lodge", "complaint", "police", "report", "first information"],
    question: "How to file an FIR?",
    answer: `**How to File an FIR (First Information Report)**

**Step 1: Go to the nearest Police Station**
- Any police station can register your FIR (Zero FIR concept under BNSS 2023)
- You can also file online through your state's police portal

**Step 2: Provide Information**
- Describe the incident clearly
- Mention date, time, and place
- Names of accused (if known)
- Names of witnesses (if any)

**Step 3: Get Your Copy**
- Police MUST give you a free copy of FIR
- Note down the FIR number
- If refused, approach SP/DCP or file complaint with State Human Rights Commission

**Important Rights:**
- Police cannot refuse to file FIR for cognizable offenses (Section 173 BNSS)
- You can send FIR by post, email, or WhatsApp
- Women can give statement at home
- FIR can be filed in your language

**If Police Refuses:**
1. Write to Superintendent of Police
2. Approach Magistrate under Section 175 BNSS
3. File complaint with State/National Human Rights Commission

**Legal Reference:** Section 173 BNSS 2023 (earlier Section 154 CrPC)`
  },
  {
    keywords: ["zero fir", "any police station", "jurisdiction"],
    question: "What is Zero FIR?",
    answer: `**Zero FIR - File FIR at ANY Police Station**

Under Section 173 BNSS 2023 (earlier Section 154 CrPC), you can file an FIR at ANY police station in India, regardless of where the crime occurred.

**How it works:**
1. Go to nearest police station
2. File your complaint (Zero FIR)
3. Police registers it with "0" number
4. FIR is transferred to correct jurisdiction within 24 hours

**Your Rights:**
- Cannot be denied based on jurisdiction
- Especially important for women's safety, accidents
- Useful when traveling or in emergency

**Example:** If you're robbed in Delhi but are in Mumbai, file Zero FIR in Mumbai. It will be transferred to Delhi police.`
  },
  // Bail Related
  {
    keywords: ["bail", "arrest", "custody", "release", "jail"],
    question: "How to get bail?",
    answer: `**Bail in India - Complete Guide**

**Types of Bail:**

1. **Regular Bail** (Section 480 BNSS)
   - Apply after arrest
   - File in Sessions Court or High Court

2. **Anticipatory Bail** (Section 482 BNSS)
   - Before arrest, if you fear arrest
   - Apply in Sessions Court or High Court

3. **Interim Bail**
   - Temporary bail till final hearing
   - Usually for medical emergencies

**Bailable vs Non-Bailable Offenses:**

| Type | Bail | Examples |
|------|------|----------|
| Bailable | Matter of right | Theft, cheating, hurt |
| Non-Bailable | Court's discretion | Murder, rape, dacoity |

**Bail Rights:**
- For bailable offense: Police MUST grant bail
- Women/children: Special consideration
- If charge sheet not filed in 60/90 days: Default bail (Section 187 BNSS)

**Documents Needed:**
- FIR copy
- Arrest memo
- ID proof
- Address proof
- Surety (local person with property)

**Cost:** ₹5,000 - ₹50,000 (lawyer fees) + surety bond`
  },
  // Domestic Violence
  {
    keywords: ["domestic violence", "wife beating", "husband", "cruelty", "dowry", "harassment", "498a", "dvact"],
    question: "What are laws against domestic violence?",
    answer: `**Domestic Violence Laws in India**

**1. Section 86 BNS 2023 (Earlier 498A IPC) - Cruelty**
- Punishment: Up to 3 years + fine
- Covers physical AND mental cruelty
- Cognizable, non-bailable

**2. Protection of Women from Domestic Violence Act, 2005**
- Civil law for protection
- Get Protection Order, Residence Order, Monetary Relief
- Can stay in shared household

**What is Domestic Violence?**
- Physical abuse (beating, hitting)
- Emotional abuse (insults, threats)
- Economic abuse (denying money, job)
- Sexual abuse
- Dowry demands

**How to File Complaint:**
1. **Police:** File FIR under Section 86 BNS
2. **Protection Officer:** Apply for protection order
3. **Magistrate Court:** File under DV Act
4. **Women's Cell:** Get counseling and help

**Helplines:**
- Women Helpline: 181
- NCW Helpline: 7827-170-170
- Police: 100/112

**Your Rights:**
- Cannot be thrown out of home
- Right to live in shared household
- Right to maintenance
- Right to custody of children`
  },
  // Property Disputes
  {
    keywords: ["property", "land", "inheritance", "will", "succession", "ancestral", "partition"],
    question: "What are property rights and inheritance laws?",
    answer: `**Property & Inheritance Laws in India**

**Ancestral Property:**
- Property inherited from father/grandfather/great-grandfather
- Both sons AND daughters have EQUAL rights (since 2005 amendment)
- Daughter's right: Birth right, cannot be denied

**Self-Acquired Property:**
- Owner can give to anyone by Will
- Without Will: Legal heirs inherit equally

**Hindu Succession Act, 1956 (Amended 2005):**

| Heir Class | Who |
|------------|-----|
| Class I | Son, daughter, widow, mother |
| Class II | Father, siblings |

**Daughter's Rights:**
- Equal share as son in ancestral property
- Right by birth (not marriage)
- Married daughter also has rights
- Can demand partition

**How to Claim Property:**
1. Get legal heir certificate
2. Mutate property records
3. If dispute: File civil suit for partition

**Important Documents:**
- Sale deed / Title deed
- Khata/Patta
- Encumbrance certificate
- Legal heir certificate

**Common Issues:**
- Illegal possession: File suit for recovery
- Disputed will: Challenge in court
- Denial by brothers: File partition suit`
  },
  // Tenant Rights
  {
    keywords: ["rent", "tenant", "landlord", "eviction", "agreement", "lease", "deposit"],
    question: "What are tenant rights in India?",
    answer: `**Tenant Rights in India**

**Rent Agreement Essentials:**
- Get written agreement (11 months + renewable)
- Register if more than 11 months
- Keep copy safe

**Your Rights as Tenant:**
1. **Cannot be evicted without notice** (usually 1-3 months)
2. **Security deposit refund** (deduct only actual damages)
3. **Essential services** cannot be cut (water, electricity)
4. **Privacy** - landlord needs permission to enter
5. **Receipt for rent** - always take receipt

**Landlord CANNOT:**
- Increase rent arbitrarily (check local Rent Control Act)
- Cut water/electricity
- Lock you out without court order
- Enter without permission

**Eviction Process (Legal):**
1. Landlord must give written notice
2. File eviction suit in Rent Court
3. Court hearing and order
4. Only then can evict

**If Illegally Evicted:**
- Call police (it's criminal offense)
- File complaint under Section 441 BNS (trespass)
- Approach Rent Controller

**Security Deposit:**
- Standard: 2-3 months rent
- Must be returned within 1-2 months of leaving
- Deductions only for actual damage (not wear & tear)`
  },
  // Consumer Rights
  {
    keywords: ["consumer", "product", "defective", "refund", "complaint", "fraud", "cheating", "warranty"],
    question: "How to file consumer complaint?",
    answer: `**Consumer Rights & Complaint Filing**

**Your Rights under Consumer Protection Act, 2019:**
1. Right to safety
2. Right to information
3. Right to choose
4. Right to be heard
5. Right to redressal
6. Right to consumer education

**Where to Complain:**

| Amount | Forum |
|--------|-------|
| Up to ₹1 Crore | District Forum |
| ₹1-10 Crore | State Commission |
| Above ₹10 Crore | National Commission |

**How to File Complaint:**
1. **Online:** consumerhelpline.gov.in
2. **Offline:** District Consumer Forum

**Documents Needed:**
- Bill/Invoice/Receipt
- Warranty card
- Product photos
- Communication with seller
- Bank statement (if applicable)

**Timeline:**
- File within 2 years of problem
- Hearing within 90 days
- Order within 3-5 months

**E-commerce Issues:**
- 7-day return policy (most products)
- Full refund if defective
- Seller + Platform both liable

**Helpline:** 1915 (National Consumer Helpline)`
  },
  // Motor Vehicle Accident
  {
    keywords: ["accident", "vehicle", "car", "bike", "hit", "run", "compensation", "motor", "road"],
    question: "What to do after a road accident?",
    answer: `**Road Accident - Legal Steps**

**Immediate Steps:**
1. **Help the injured** - take to hospital
2. **Call police** - 100/112
3. **Note vehicle number** of other party
4. **Take photos** of accident scene
5. **Get witness contacts**

**Good Samaritan Protection:**
- You CANNOT be harassed for helping accident victim
- Hospital must treat (free for first 48 hours)
- No police questioning of helper

**Compensation Claims:**

**1. Insurance Claim:**
- Inform insurance within 24 hours
- File claim with Motor Accident Claims Tribunal (MACT)

**2. Criminal Case:**
- Section 281 BNS: Rash driving causing death
- Section 106 BNS: Death by negligence
- Hit & Run: Enhanced punishment

**Compensation Amount:**
| Type | Amount |
|------|--------|
| Death (hit & run) | ₹2 lakh (immediate) + more through MACT |
| Injury | Based on disability % and income |
| Vehicle damage | As per surveyor |

**MACT Process:**
1. File application within limitation (3 years for injury)
2. Submit documents (FIR, medical, income proof)
3. Tribunal decides compensation
4. Insurance company pays

**Documents Needed:**
- FIR copy
- Medical bills
- Disability certificate
- Income proof
- Vehicle RC, license, insurance`
  },
  // Cyber Crime
  {
    keywords: ["cyber", "online", "hack", "fraud", "otp", "scam", "bank", "upi", "phishing"],
    question: "How to report cyber crime?",
    answer: `**Cyber Crime Reporting**

**Types of Cyber Crimes:**
- Online fraud (UPI, banking)
- Hacking
- Identity theft
- Cyberstalking
- Online harassment
- Morphed photos

**How to Report:**

**1. Online Portal:**
- cybercrime.gov.in (National Portal)
- Report within 72 hours for best recovery chance

**2. Cyber Crime Cell:**
- Visit nearest cyber cell
- Bring all evidence

**3. Bank:**
- Call bank immediately
- Block cards/UPI
- File dispute

**Evidence to Collect:**
- Screenshots of messages/transactions
- Transaction IDs
- Phone numbers/email IDs
- Bank statements
- URLs of fake websites

**Laws:**
- IT Act, 2000 (Sections 66, 66C, 66D)
- Section 318 BNS (Cheating)

**Money Recovery:**
- Report within 24-48 hours: Higher chance
- Bank must resolve in 90 days
- If bank's fault: Full refund

**Helplines:**
- Cyber Crime: 1930
- cybercrime.gov.in
- Bank fraud: Your bank's helpline`
  },
  // Women's Rights
  {
    keywords: ["women", "sexual", "harassment", "workplace", "posh", "stalking", "eve teasing"],
    question: "What are laws protecting women?",
    answer: `**Laws Protecting Women in India**

**1. Sexual Harassment at Workplace (POSH Act, 2013)**
- Every office with 10+ employees must have ICC
- Complaint within 3 months
- Inquiry within 90 days

**2. Section 74 BNS (Earlier 354 IPC) - Outraging Modesty**
- Eve teasing, touching, gestures
- Punishment: 1-5 years

**3. Section 78 BNS - Stalking**
- Following, contacting repeatedly
- Punishment: Up to 3 years (first offense)

**4. Section 79 BNS - Voyeurism**
- Recording/watching woman in private
- Punishment: 1-3 years

**5. Section 63-69 BNS - Rape Laws**
- Stringent punishment (10 years to death)
- Special courts for fast trial

**6. Dowry Prohibition Act**
- Giving/taking dowry: Up to 5 years

**Where to Complain:**
- Police: 100/112
- Women Helpline: 181
- NCW: 7827-170-170
- One Stop Centre: Nearest hospital

**Rights During Investigation:**
- Woman officer for statement
- Statement at home if needed
- Medical exam by female doctor
- Free legal aid
- Identity protection`
  },
  // Arrest Rights
  {
    keywords: ["arrest", "rights", "police", "custody", "detention", "lawyer"],
    question: "What are my rights if arrested?",
    answer: `**Your Rights When Arrested**

**Constitutional Rights:**

1. **Right to know reason** (Article 22)
   - Police MUST tell why you're arrested

2. **Right to lawyer** (Article 22)
   - Can contact lawyer immediately
   - Free legal aid if can't afford

3. **Right to inform family**
   - Police must inform relative/friend
   - Within 8-12 hours of arrest

4. **Right against torture**
   - No physical or mental torture
   - Can file complaint against police

5. **Right to medical exam**
   - If injured during arrest

**Important Rules:**

| Rule | Provision |
|------|-----------|
| Arrest memo | Must be given |
| Woman's arrest | Only by female officer, only 6am-6pm |
| Produce before Magistrate | Within 24 hours |
| Handcuffs | Only for serious offenders |

**What Police Cannot Do:**
- Beat or torture you
- Arrest without warrant (for bailable offenses)
- Keep beyond 24 hours without court order
- Deny lawyer access

**If Rights Violated:**
- File complaint with senior officer
- Approach High Court (Habeas Corpus)
- File with Human Rights Commission
- Sue for compensation

**Emergency Contacts:**
- Legal Services Authority (for free lawyer)
- NHRC: 1800-345-4545`
  },
  // Divorce
  {
    keywords: ["divorce", "separation", "marriage", "alimony", "maintenance", "custody", "mutual consent"],
    question: "How to file for divorce?",
    answer: `**Divorce Laws in India**

**Types of Divorce:**

**1. Mutual Consent (Section 13B Hindu Marriage Act)**
- Both agree to separate
- 6 months cooling period
- Faster and cheaper

**2. Contested Divorce**
- One party doesn't agree
- Takes 2-5 years
- Need to prove grounds

**Grounds for Divorce:**
- Cruelty (physical/mental)
- Adultery
- Desertion (2+ years)
- Mental disorder
- Communicable disease
- Conversion to another religion

**Process:**

**Mutual Consent:**
1. File joint petition
2. First hearing
3. 6 months cooling period
4. Second hearing
5. Decree granted

**Contested:**
1. File petition
2. Summons to spouse
3. Evidence & arguments
4. Judgment (may take years)

**Maintenance/Alimony:**
- Wife entitled to maintenance during and after divorce
- Amount: Based on husband's income, wife's needs
- Usually 20-30% of husband's income

**Child Custody:**
- Best interest of child
- Below 5 years: Usually mother
- Visitation rights to other parent

**Documents:**
- Marriage certificate
- Address proof
- Income proof
- Evidence of grounds (if contested)`
  },
  // RTI
  {
    keywords: ["rti", "information", "government", "right to information", "public"],
    question: "How to file RTI application?",
    answer: `**Right to Information (RTI) - Complete Guide**

**What is RTI?**
- Right to get information from government departments
- Any Indian citizen can file
- Applies to Central, State, and Local bodies

**How to File:**

**Online:** rtionline.gov.in (for Central govt)
**Offline:** Write application + ₹10 fee

**Application Format:**
\`\`\`
To: Public Information Officer
[Department Name]

Subject: Application under RTI Act, 2005

I wish to obtain following information:
1. [Your question 1]
2. [Your question 2]

Fee: ₹10 enclosed/paid online

Name:
Address:
Date:
\`\`\`

**Fees:**
- Application: ₹10
- Per page copy: ₹2
- BPL: Free

**Timeline:**
| Stage | Time |
|-------|------|
| Reply | 30 days |
| If life/liberty | 48 hours |
| First Appeal | 30 days |
| Second Appeal (CIC) | 90 days |

**If No Reply:**
1. First Appeal to senior officer
2. Second Appeal to Information Commission
3. Penalty on PIO: ₹250/day up to ₹25,000

**Tips:**
- Be specific in questions
- One subject per application
- Keep copies of everything`
  },
];

// Search function with keyword matching
export function searchLegalKnowledge(query) {
  const queryLower = query.toLowerCase();
  const queryWords = queryLower.split(/\s+/).filter(w => w.length > 2);

  let bestMatch = null;
  let bestScore = 0;

  for (const qa of LEGAL_QA) {
    let score = 0;

    // Check keyword matches
    for (const keyword of qa.keywords) {
      if (queryLower.includes(keyword)) {
        score += 10;
      }
      for (const word of queryWords) {
        if (keyword.includes(word) || word.includes(keyword)) {
          score += 5;
        }
      }
    }

    // Check question similarity
    const questionLower = qa.question.toLowerCase();
    for (const word of queryWords) {
      if (questionLower.includes(word)) {
        score += 3;
      }
    }

    if (score > bestScore) {
      bestScore = score;
      bestMatch = qa;
    }
  }

  // Return best match if score is good enough
  if (bestScore >= 5 && bestMatch) {
    return {
      found: true,
      answer: bestMatch.answer,
      question: bestMatch.question,
      score: bestScore
    };
  }

  return { found: false };
}

// Search BNS sections
export function searchBNSSection(query) {
  const queryLower = query.toLowerCase();
  const results = [];

  for (const [section, data] of Object.entries(BNS_SECTIONS)) {
    const matchTitle = data.title.toLowerCase().includes(queryLower);
    const matchDesc = data.description.toLowerCase().includes(queryLower);
    const matchSection = queryLower.includes(section) || queryLower.includes(data.oldIPC);

    if (matchTitle || matchDesc || matchSection) {
      results.push({
        section,
        ...data,
        formatted: `**Section ${section} BNS (Earlier IPC ${data.oldIPC})**\n**${data.title}**\n\n${data.description}\n\n**Punishment:** ${data.punishment}`
      });
    }
  }

  return results;
}

// Main offline legal assistant function
export function getOfflineLegalAnswer(query, language = 'english') {
  // First try Q&A database
  const qaResult = searchLegalKnowledge(query);

  if (qaResult.found) {
    let answer = qaResult.answer;

    // Add language-specific prefix
    if (language === 'hindi') {
      answer = `📖 **संबंधित विषय:** ${qaResult.question}\n\n${answer}\n\n⚠️ **नोट:** जटिल मामलों के लिए वकील से परामर्श करें।`;
    } else if (language === 'kannada') {
      answer = `📖 **ಸಂಬಂಧಿತ ವಿಷಯ:** ${qaResult.question}\n\n${answer}\n\n⚠️ **ಸೂಚನೆ:** ಸಂಕೀರ್ಣ ಪ್ರಕರಣಗಳಿಗೆ ವಕೀಲರನ್ನು ಸಂಪರ್ಕಿಸಿ.`;
    } else if (language === 'marathi') {
      answer = `📖 **संबंधित विषय:** ${qaResult.question}\n\n${answer}\n\n⚠️ **टीप:** गुंतागुंतीच्या प्रकरणांसाठी वकिलांशी सल्लामसलत करा.`;
    } else {
      answer = `📖 **Related Topic:** ${qaResult.question}\n\n${answer}\n\n⚠️ **Disclaimer:** For complex cases, please consult a qualified lawyer.`;
    }

    return {
      success: true,
      answer,
      source: 'knowledge_base'
    };
  }

  // Try BNS section search
  const bnsResults = searchBNSSection(query);
  if (bnsResults.length > 0) {
    const answer = bnsResults.map(r => r.formatted).join('\n\n---\n\n');
    return {
      success: true,
      answer: `📜 **Found ${bnsResults.length} relevant section(s):**\n\n${answer}`,
      source: 'bns_sections'
    };
  }

  // No match found
  const noMatchMessages = {
    english: `I couldn't find a specific answer in my offline database for: "${query}"\n\n**Suggestions:**\n- Try keywords like: FIR, bail, divorce, property, consumer, arrest, domestic violence\n- Ask about specific laws: BNS, IPC, BNSS, CrPC\n- Connect to internet for AI-powered answers\n\n**Helplines:**\n- Police: 100\n- Women: 181\n- Legal Aid: 15100`,
    hindi: `मुझे अपने ऑफलाइन डेटाबेस में "${query}" का विशिष्ट उत्तर नहीं मिला।\n\n**सुझाव:**\n- ये शब्द आज़माएं: FIR, जमानत, तलाक, संपत्ति, उपभोक्ता\n- इंटरनेट कनेक्ट करें AI उत्तर के लिए\n\n**हेल्पलाइन:**\n- पुलिस: 100\n- महिला: 181`,
    kannada: `"${query}" ಗೆ ನನ್ನ ಆಫ್‌ಲೈನ್ ಡೇಟಾಬೇಸ್‌ನಲ್ಲಿ ನಿರ್ದಿಷ್ಟ ಉತ್ತರ ಸಿಗಲಿಲ್ಲ।\n\n**ಸಹಾಯವಾಣಿ:**\n- ಪೊಲೀಸ್: 100\n- ಮಹಿಳಾ: 181`,
    marathi: `माझ्या ऑफलाइन डेटाबेसमध्ये "${query}" साठी विशिष्ट उत्तर सापडले नाही।\n\n**मदत:**\n- पोलीस: 100\n- महिला: 181`
  };

  return {
    success: true,
    answer: noMatchMessages[language] || noMatchMessages.english,
    source: 'no_match'
  };
}

export default { searchLegalKnowledge, searchBNSSection, getOfflineLegalAnswer, BNS_SECTIONS, LEGAL_QA };
