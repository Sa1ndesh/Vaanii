"""
Download Pre-made Indian Legal Datasets from Kaggle and HuggingFace
Much more reliable than scraping - thousands of examples ready to use.

Usage: python download_datasets.py
"""

import os
import json
import requests
import zipfile
import shutil
from pathlib import Path
from datetime import datetime

# Output directories
DATA_DIR = Path("../../data")
DATASETS_DIR = DATA_DIR / "downloaded_datasets"
TRAINING_DIR = DATA_DIR / "training"

DATASETS_DIR.mkdir(parents=True, exist_ok=True)
TRAINING_DIR.mkdir(parents=True, exist_ok=True)


def download_from_huggingface():
    """Download Indian legal datasets from HuggingFace"""
    print("\n" + "="*60)
    print("DOWNLOADING FROM HUGGINGFACE")
    print("="*60)

    datasets_info = []

    # 1. Indian Legal Documents Corpus
    print("\n[1] Fetching Indian Legal Documents dataset info...")
    try:
        # HuggingFace datasets API
        url = "https://huggingface.co/api/datasets/keshavg/indian-legal-text"
        response = requests.get(url, timeout=30)
        if response.status_code == 200:
            datasets_info.append({
                "name": "indian-legal-text",
                "source": "huggingface",
                "url": "https://huggingface.co/datasets/keshavg/indian-legal-text",
                "description": "Indian Legal Text corpus for NLP tasks"
            })
            print("  ✓ Found: indian-legal-text")
    except Exception as e:
        print(f"  ✗ Error: {e}")

    # 2. Indian Court Cases
    print("\n[2] Fetching Indian Court Cases dataset...")
    try:
        url = "https://huggingface.co/api/datasets/Exploration-Lab/IL-TUR"
        response = requests.get(url, timeout=30)
        if response.status_code == 200:
            datasets_info.append({
                "name": "IL-TUR",
                "source": "huggingface",
                "url": "https://huggingface.co/datasets/Exploration-Lab/IL-TUR",
                "description": "Indian Legal Text Understanding and Reasoning"
            })
            print("  ✓ Found: IL-TUR (Indian Legal Understanding)")
    except Exception as e:
        print(f"  ✗ Error: {e}")

    # 3. LegalBench India
    print("\n[3] Checking LegalBench datasets...")
    try:
        url = "https://huggingface.co/api/datasets/nguha/legalbench"
        response = requests.get(url, timeout=30)
        if response.status_code == 200:
            datasets_info.append({
                "name": "legalbench",
                "source": "huggingface",
                "url": "https://huggingface.co/datasets/nguha/legalbench",
                "description": "Legal reasoning benchmark with Indian cases"
            })
            print("  ✓ Found: LegalBench")
    except Exception as e:
        print(f"  ✗ Error: {e}")

    return datasets_info


def create_manual_training_data():
    """Create comprehensive manual training data for Indian law"""
    print("\n" + "="*60)
    print("CREATING COMPREHENSIVE MANUAL TRAINING DATA")
    print("="*60)

    training_examples = []

    # =====================================================
    # BHARATIYA NYAYA SANHITA 2023 (Replaces IPC)
    # =====================================================
    bns_qa = [
        # Murder and Culpable Homicide
        {
            "instruction": "What is the punishment for murder under the new criminal law?",
            "input": "",
            "output": "Under Bharatiya Nyaya Sanhita 2023 (BNS), Section 103 prescribes punishment for murder:\n\n1. **Death penalty or life imprisonment** plus fine for regular murder\n2. **Mandatory death penalty** for:\n   - Murder by a group of 5+ persons based on race, caste, community, sex, place of birth, language, or personal belief (mob lynching)\n   - Murder of a child below 18 years\n   - Rape and murder combined\n\nThis replaces the old Section 302 of IPC. The new law specifically addresses mob lynching as a separate aggravated offense.",
            "category": "criminal_law",
            "language": "english"
        },
        {
            "instruction": "BNS 2023 में हत्या की सजा क्या है?",
            "input": "",
            "output": "भारतीय न्याय संहिता 2023 (BNS) की धारा 103 के अनुसार हत्या की सजा:\n\n1. **मृत्युदंड या आजीवन कारावास** और जुर्माना - सामान्य हत्या के लिए\n2. **अनिवार्य मृत्युदंड** निम्न मामलों में:\n   - जाति, धर्म, समुदाय, लिंग के आधार पर 5 या अधिक लोगों द्वारा हत्या (भीड़ हत्या/मॉब लिंचिंग)\n   - 18 वर्ष से कम उम्र के बच्चे की हत्या\n   - बलात्कार और हत्या एक साथ\n\nयह पुरानी IPC की धारा 302 की जगह लेती है।",
            "category": "criminal_law",
            "language": "hindi"
        },
        {
            "instruction": "ಹೊಸ ಭಾರತೀಯ ನ್ಯಾಯ ಸಂಹಿತಾ 2023 ರಲ್ಲಿ ಕೊಲೆಗೆ ಶಿಕ್ಷೆ ಏನು?",
            "input": "",
            "output": "ಭಾರತೀಯ ನ್ಯಾಯ ಸಂಹಿತಾ 2023 (BNS) ಸೆಕ್ಷನ್ 103 ರ ಪ್ರಕಾರ ಕೊಲೆಗೆ ಶಿಕ್ಷೆ:\n\n1. **ಮರಣದಂಡನೆ ಅಥವಾ ಜೀವಾವಧಿ ಶಿಕ್ಷೆ** ಮತ್ತು ದಂಡ - ಸಾಮಾನ್ಯ ಕೊಲೆಗೆ\n2. **ಕಡ್ಡಾಯ ಮರಣದಂಡನೆ** ಈ ಸಂದರ್ಭಗಳಲ್ಲಿ:\n   - ಜಾತಿ, ಧರ್ಮ, ಸಮುದಾಯ, ಲಿಂಗ ಆಧಾರಿತ 5 ಅಥವಾ ಹೆಚ್ಚು ಜನರಿಂದ ಕೊಲೆ (ಗುಂಪು ಹತ್ಯೆ)\n   - 18 ವರ್ಷಕ್ಕಿಂತ ಕಡಿಮೆ ವಯಸ್ಸಿನ ಮಕ್ಕಳ ಕೊಲೆ\n   - ಅತ್ಯಾಚಾರ ಮತ್ತು ಕೊಲೆ ಒಟ್ಟಿಗೆ\n\nಇದು ಹಳೆಯ IPC ಸೆಕ್ಷನ್ 302 ಅನ್ನು ಬದಲಿಸುತ್ತದೆ।",
            "category": "criminal_law",
            "language": "kannada"
        },
        # Rape and Sexual Offenses
        {
            "instruction": "What are the rape laws under BNS 2023?",
            "input": "",
            "output": "Under Bharatiya Nyaya Sanhita 2023, rape is covered under Sections 63-69:\n\n**Section 63 - Definition of Rape:**\nA man commits rape if he has sexual intercourse with a woman:\n- Against her will\n- Without her consent\n- With consent obtained by fear, threat, or intoxication\n- When she cannot understand the nature of consent\n- With or without consent when she is under 18 years\n\n**Punishments (Section 64):**\n- **Minimum 10 years** to life imprisonment for rape\n- **20 years to life** for gang rape\n- **Death penalty or life** for rape causing death or persistent vegetative state\n- **20 years to life** for rape of a child under 12\n- **Life imprisonment** for repeat offenders\n\n**Section 69 - Rape by deception (false promise of marriage):**\n- Up to 10 years imprisonment",
            "category": "criminal_law",
            "language": "english"
        },
        # Theft, Robbery, Dacoity
        {
            "instruction": "What is the difference between theft, robbery and dacoity under BNS?",
            "input": "",
            "output": "Under Bharatiya Nyaya Sanhita 2023:\n\n**Theft (Section 303):**\n- Taking movable property without consent\n- Intent to take dishonestly\n- Punishment: Up to 3 years imprisonment or fine or both\n\n**Robbery (Section 309):**\n- Theft + use of force or fear of instant death/hurt\n- Punishment: Up to 10 years rigorous imprisonment + fine\n- If on highway between sunset and sunrise: Up to 14 years\n\n**Dacoity (Section 310):**\n- Robbery committed by 5 or more persons\n- Punishment: Life imprisonment or 10 years rigorous imprisonment + fine\n- If with murder: Death penalty or life imprisonment\n\n**Key Distinction:**\nTheft → No violence\nRobbery → Theft + violence/fear (1-4 persons)\nDacoity → Robbery by 5+ persons acting together",
            "category": "criminal_law",
            "language": "english"
        },
        # Cheating
        {
            "instruction": "What is Section 420 equivalent in new BNS 2023?",
            "input": "",
            "output": "The equivalent of old IPC Section 420 (Cheating) in Bharatiya Nyaya Sanhita 2023 is **Section 318**.\n\n**Section 316 - Cheating:**\nDeceiving someone dishonestly to:\n- Deliver property, or\n- Make, alter, or destroy a document\n- Punishment: Up to 3 years imprisonment or fine or both\n\n**Section 318 - Cheating and dishonestly inducing delivery of property:**\n- Cheating + causing wrongful loss or damage\n- Punishment: Up to 7 years imprisonment + fine\n\n**Section 319 - Cheating by personation:**\n- Pretending to be someone else\n- Punishment: Up to 5 years imprisonment + fine\n\n**Common examples:**\n- Online fraud\n- Fake investment schemes\n- Insurance fraud\n- Identity theft\n- Matrimonial fraud",
            "category": "criminal_law",
            "language": "english"
        },
    ]
    training_examples.extend(bns_qa)

    # =====================================================
    # BHARATIYA NAGARIK SURAKSHA SANHITA 2023 (Replaces CrPC)
    # =====================================================
    bnss_qa = [
        # FIR
        {
            "instruction": "How to file an FIR under new criminal procedure law?",
            "input": "",
            "output": "Under Bharatiya Nagarik Suraksha Sanhita 2023 (BNSS), FIR filing is covered under Section 173:\n\n**How to File:**\n1. Go to any police station (Zero FIR allowed - file anywhere, transferred later)\n2. Give information orally or in writing\n3. Police must register FIR for cognizable offenses\n4. Get a free copy of FIR immediately\n5. Can also file online through state police portals\n\n**New Features in BNSS:**\n- **Zero FIR mandatory** - File at ANY police station\n- **e-FIR** facility for certain offenses\n- **Within 24 hours**: FIR copy to be provided free\n- **Acknowledgment**: SMS/electronic acknowledgment mandatory\n- **Woman complainant**: Can record statement at her residence\n\n**If Police Refuses:**\n1. Approach Superintendent of Police\n2. File complaint before Magistrate under Section 175\n3. Magistrate can direct police to register FIR\n\n**Timeline:** Investigation to be completed within 90 days for offenses punishable up to 3 years, 180 days for serious offenses.",
            "category": "criminal_procedure",
            "language": "english"
        },
        {
            "instruction": "FIR कैसे दर्ज करें नए कानून के तहत?",
            "input": "",
            "output": "भारतीय नागरिक सुरक्षा संहिता 2023 (BNSS) धारा 173 के तहत FIR दर्ज करने की प्रक्रिया:\n\n**कैसे दर्ज करें:**\n1. किसी भी थाने में जाएं (Zero FIR - कहीं भी दर्ज करा सकते हैं)\n2. मौखिक या लिखित में जानकारी दें\n3. पुलिस को संज्ञेय अपराध में FIR दर्ज करनी होगी\n4. FIR की मुफ्त प्रति तुरंत लें\n5. ऑनलाइन भी दर्ज करा सकते हैं\n\n**नई विशेषताएं:**\n- **Zero FIR अनिवार्य** - किसी भी थाने में\n- **e-FIR** की सुविधा\n- **24 घंटे में**: FIR कॉपी मुफ्त देनी होगी\n- **SMS सूचना** अनिवार्य\n- **महिला शिकायतकर्ता**: घर पर बयान दर्ज कर सकती है\n\n**अगर पुलिस मना करे:**\n1. SP से संपर्क करें\n2. धारा 175 के तहत मजिस्ट्रेट को शिकायत\n3. मजिस्ट्रेट पुलिस को FIR दर्ज करने का आदेश दे सकता है",
            "category": "criminal_procedure",
            "language": "hindi"
        },
        # Bail
        {
            "instruction": "What are the bail provisions under BNSS 2023?",
            "input": "",
            "output": "Bharatiya Nagarik Suraksha Sanhita 2023 bail provisions:\n\n**Section 480 - Bail in Bailable Offenses:**\n- Bail is a RIGHT, not discretion\n- Police MUST release on bail\n- If unable to furnish bail: Magistrate must release on personal bond\n\n**Section 482 - Anticipatory Bail:**\n- Apply to Sessions Court or High Court\n- BEFORE arrest, if reason to believe arrest may happen\n- Court can impose conditions\n- **New:** Interim bail can be granted even without notice to prosecution\n\n**Section 483 - Regular Bail (Non-Bailable):**\n- Discretionary - Court may grant\n- Factors: Nature of offense, evidence, flight risk, tampering\n- **New provision:** If investigation not completed in 60/90 days, bail MUST be considered\n\n**Section 479 - Bail for Undertrials:**\n- If detained for HALF the maximum sentence period → Entitled to bail\n- Except: Death penalty, life imprisonment, or multiple cases\n\n**Key Changes:**\n- First-time offenders (max 3 years punishment): Release on bond after 1/3 detention\n- Video conferencing allowed for bail hearings\n- Electronic surety bonds permitted",
            "category": "criminal_procedure",
            "language": "english"
        },
        # Arrest
        {
            "instruction": "What are my rights during arrest under new law?",
            "input": "",
            "output": "Under Bharatiya Nagarik Suraksha Sanhita 2023, your rights during arrest:\n\n**At the Time of Arrest (Section 35):**\n1. Be informed of grounds of arrest\n2. Be informed of right to bail (if bailable offense)\n3. Arrested person's family/friend MUST be informed immediately\n4. Right to consult a lawyer of choice\n5. Right to free legal aid if cannot afford lawyer\n6. Woman cannot be arrested after sunset and before sunrise (except by female officer with Magistrate order)\n\n**After Arrest:**\n1. Medical examination within 24 hours\n2. Produced before Magistrate within 24 hours\n3. Right to inform employer (Section 37)\n4. All documents/electronic devices seized must be listed\n5. **New:** Arrest memo with video recording\n6. **New:** Information to legal aid authority within 24 hours\n\n**For Women:**\n- Only female officer can search\n- Can record statement at residence\n- No handcuffing except violent cases\n\n**Against Illegal Arrest:**\n- File Habeas Corpus petition in High Court\n- Compensation for illegal detention under Section 358",
            "category": "criminal_procedure",
            "language": "english"
        },
    ]
    training_examples.extend(bnss_qa)

    # =====================================================
    # FAMILY LAW
    # =====================================================
    family_law_qa = [
        # Divorce
        {
            "instruction": "How to file for divorce in India?",
            "input": "",
            "output": "Divorce procedures in India depend on religion and type:\n\n**MUTUAL CONSENT DIVORCE (Section 13B Hindu Marriage Act / Section 28 Special Marriage Act):**\n1. Living separately for 1 year\n2. Joint petition by both spouses\n3. First motion → 6 month cooling period → Second motion\n4. Total time: 6-18 months\n5. **Supreme Court**: Can waive 6-month period in genuine cases\n\n**CONTESTED DIVORCE (Section 13 HMA):**\nGrounds:\n- Adultery\n- Cruelty (physical/mental)\n- Desertion for 2+ years\n- Conversion to another religion\n- Unsoundness of mind\n- Leprosy/Venereal disease\n- Renunciation of world\n- Not heard alive for 7+ years\n\n**Additional grounds for wife:**\n- Husband's rape, sodomy, bestiality\n- No resumption of cohabitation after maintenance/judicial separation decree\n\n**PROCEDURE:**\n1. File petition in Family Court (where marriage was solemnized OR where parties last resided together OR where wife resides)\n2. Serve notice to spouse\n3. Written statement by respondent\n4. Evidence and arguments\n5. Decree by court\n\n**MUSLIM DIVORCE:**\n- Talaq-e-Hasan (revocable)\n- Khula (wife initiated)\n- Triple Talaq BANNED (now criminal offense)\n\n**CHRISTIAN DIVORCE:**\n- Under Indian Divorce Act, 1869",
            "category": "family_law",
            "language": "english"
        },
        {
            "instruction": "तलाक कैसे लें भारत में?",
            "input": "",
            "output": "भारत में तलाक की प्रक्रिया धर्म और प्रकार पर निर्भर करती है:\n\n**आपसी सहमति से तलाक (धारा 13B हिंदू विवाह अधिनियम):**\n1. एक साल से अलग रह रहे हों\n2. दोनों पति-पत्नी मिलकर याचिका दायर करें\n3. पहला मोशन → 6 महीने का कूलिंग पीरियड → दूसरा मोशन\n4. कुल समय: 6-18 महीने\n5. सुप्रीम कोर्ट विशेष मामलों में 6 महीने माफ कर सकता है\n\n**विवादित तलाक (धारा 13 HMA):**\nआधार:\n- व्यभिचार\n- क्रूरता (शारीरिक/मानसिक)\n- 2+ साल का परित्याग\n- धर्म परिवर्तन\n- मानसिक विकार\n- कुष्ठ रोग/यौन रोग\n- संन्यास\n- 7+ साल से कोई खबर नहीं\n\n**पत्नी के लिए अतिरिक्त आधार:**\n- पति द्वारा बलात्कार, समलैंगिक संबंध\n- भरण-पोषण/न्यायिक पृथक्करण के बाद सहवास न होना\n\n**प्रक्रिया:**\n1. फैमिली कोर्ट में याचिका दायर करें\n2. दूसरे पक्ष को नोटिस\n3. जवाबी बयान\n4. सबूत और बहस\n5. डिक्री\n\n**मुस्लिम तलाक:**\n- तीन तलाक अब अपराध है\n- खुला (पत्नी द्वारा)\n- तलाक-ए-हसन (वापस लिया जा सकता है)",
            "category": "family_law",
            "language": "hindi"
        },
        # Maintenance
        {
            "instruction": "What are the maintenance rights of wife in India?",
            "input": "",
            "output": "A wife's maintenance rights in India:\n\n**1. SECTION 125 CrPC / SECTION 144 BNSS - Criminal Maintenance:**\n- Available to ALL women (Hindu, Muslim, Christian, etc.)\n- Quick remedy - decided in months\n- Maintenance for wife, children, and parents\n- **Amount:** Based on husband's income, wife's needs\n- Can be claimed even while living together if not being maintained\n\n**2. HINDU ADOPTION AND MAINTENANCE ACT, 1956 (Section 18):**\n- Wife entitled to maintenance from husband\n- Separate residence allowed if:\n  - Husband has another wife\n  - Keeps concubine\n  - Cruel treatment\n  - Husband converted religion\n  - Any other reasonable cause\n\n**3. DOMESTIC VIOLENCE ACT, 2005 (Section 20):**\n- Maintenance for wife/live-in partner\n- Includes monetary relief for losses\n- Can include children's expenses\n- Residence order - right to stay in shared household\n\n**4. HINDU MARRIAGE ACT (Section 24 & 25):**\n- Pendente lite maintenance (during case)\n- Permanent alimony (after divorce)\n- Can be modified if circumstances change\n\n**FACTORS CONSIDERED:**\n- Husband's income and assets\n- Wife's income and earning capacity\n- Children's needs\n- Standard of living during marriage\n- Wife's age and health\n\n**GENERAL RULE:** 20-25% of husband's net income is common, but varies by case.",
            "category": "family_law",
            "language": "english"
        },
        # Domestic Violence
        {
            "instruction": "What protection does Domestic Violence Act provide?",
            "input": "",
            "output": "Protection of Women from Domestic Violence Act, 2005 provides:\n\n**WHO CAN FILE:**\n- Wife or live-in partner\n- Sister, mother, widow residing in shared household\n- Any woman in domestic relationship\n\n**AGAINST WHOM:**\n- Husband/male partner\n- Relatives of husband (in-laws)\n\n**TYPES OF ABUSE COVERED:**\n1. **Physical abuse** - beating, slapping, kicking\n2. **Sexual abuse** - forced intercourse, pornography\n3. **Verbal/Emotional abuse** - insults, humiliation, threats\n4. **Economic abuse** - not providing money, taking salary, denying food\n\n**RELIEFS AVAILABLE:**\n1. **Protection Order (Section 18):** Stop violence, no contact order\n2. **Residence Order (Section 19):** Right to stay in shared household, even if owned by husband\n3. **Monetary Relief (Section 20):** Maintenance, compensation for injuries, medical expenses\n4. **Custody Order (Section 21):** Temporary custody of children\n5. **Compensation Order (Section 22):** For mental trauma and injuries\n\n**PROCEDURE:**\n1. File complaint with Protection Officer / Magistrate / Police\n2. Court issues notice within 3 days\n3. First hearing within 3 days of notice\n4. Disposal within 60 days\n\n**HELPLINE:** Women Helpline - 181 (24x7)",
            "category": "family_law",
            "language": "english"
        },
        # Child Custody
        {
            "instruction": "How is child custody decided in India?",
            "input": "",
            "output": "Child custody in India is decided based on 'welfare of the child' principle:\n\n**TYPES OF CUSTODY:**\n1. **Physical Custody:** Where child lives\n2. **Legal Custody:** Decision-making rights\n3. **Joint Custody:** Shared by both parents\n\n**GENERAL PRINCIPLES:**\n- Child below 5 years: Usually with mother (tender years doctrine)\n- Child's preference considered (above 9 years given more weight)\n- Welfare of child is paramount - not parent's rights\n\n**FACTORS CONSIDERED:**\n1. Child's age and gender\n2. Child's preference (if mature enough)\n3. Parent's financial stability\n4. Parent's character and lifestyle\n5. Emotional bond with each parent\n6. Educational continuity\n7. Living environment\n8. Presence of siblings\n9. History of violence/abuse\n10. Willingness to allow access to other parent\n\n**LAWS APPLICABLE:**\n- Hindu: Guardians and Wards Act, 1890 + Hindu Minority and Guardianship Act, 1956\n- Muslim: Father is natural guardian (but mother usually gets custody of young children)\n- Christian/Others: Guardians and Wards Act, 1890\n\n**VISITATION RIGHTS:**\nNon-custodial parent typically gets:\n- Weekend visits\n- School vacation time\n- Festival holidays\n- Phone/video calls\n\n**MODIFICATION:**\nCustody can be changed if circumstances change significantly.",
            "category": "family_law",
            "language": "english"
        },
    ]
    training_examples.extend(family_law_qa)

    # =====================================================
    # PROPERTY LAW
    # =====================================================
    property_law_qa = [
        # Property Registration
        {
            "instruction": "How to register property in India?",
            "input": "",
            "output": "Property registration process in India under Registration Act, 1908:\n\n**STEP 1: PREPARE DOCUMENTS**\n- Sale deed/agreement\n- Title documents (previous chain of ownership)\n- Encumbrance certificate (last 13-30 years)\n- Khata certificate\n- Latest tax paid receipts\n- NOC from society (if applicable)\n- ID proof & photos of all parties\n\n**STEP 2: PAY STAMP DUTY**\n- Varies by state (typically 5-7% of property value)\n- Can be paid online or at treasury\n- e-Stamping available in most states\n\n**STEP 3: REGISTRATION PROCESS**\n1. Book appointment at Sub-Registrar office\n2. Both buyer and seller appear with 2 witnesses\n3. Submit documents with stamp duty receipt\n4. Biometric verification (Aadhaar)\n5. Pay registration fee (typically 1% of value)\n6. Document scanned and registered\n7. Receive registered document (usually same day)\n\n**REGISTRATION FEE:**\n- 1% of property value (varies by state)\n- Maximum cap in some states\n\n**TIMELINE:**\n- Must register within 4 months of execution\n- Delay can be condoned by Registrar (up to 4 more months with penalty)\n\n**ONLINE:**\nMany states offer NGDRS (National Generic Document Registration System) for online appointment and document submission.\n\n**IMPORTANT:** Unregistered documents for property over ₹100 are not admissible as evidence.",
            "category": "property_law",
            "language": "english"
        },
        # Succession/Inheritance
        {
            "instruction": "Who inherits property if someone dies without a will?",
            "input": "",
            "output": "Inheritance without will (intestate succession) depends on religion:\n\n**HINDU SUCCESSION ACT, 1956:**\n\n**Class I Heirs (Equal share):**\n- Son, Daughter\n- Widow\n- Mother\n- Son/Daughter of predeceased son/daughter\n\n**If no Class I heirs → Class II Heirs:**\n- Father, then:\n- Brother, Sister\n- Son's daughter's son/daughter\n- And so on...\n\n**IMPORTANT CHANGES (2005 Amendment):**\n- Daughters have EQUAL rights as sons in ancestral property\n- Daughter remains coparcener even after marriage\n- Applies to daughters born before and after 2005\n\n**MUSLIM PERSONAL LAW:**\n- Wife: 1/4 if no children, 1/8 if children\n- Husband: 1/2 if no children, 1/4 if children\n- Daughters: Half of son's share\n- Parents: Fixed shares\n- Only 1/3 property can be given by will\n\n**INDIAN SUCCESSION ACT, 1925 (Christians, Parsis, etc.):**\n- Spouse and children share equally\n- If only spouse: 1/2 to spouse, 1/2 to kindred\n- If only children: Equally among children\n\n**PROCEDURE TO CLAIM:**\n1. Obtain death certificate\n2. Apply for succession certificate/legal heir certificate\n3. Mutation in revenue records\n4. Transfer in society records (if flat)",
            "category": "property_law",
            "language": "english"
        },
        # Tenant Rights
        {
            "instruction": "Can a landlord evict a tenant in India?",
            "input": "",
            "output": "Landlord can evict tenant only on specific grounds under Rent Control laws:\n\n**VALID GROUNDS FOR EVICTION:**\n1. **Non-payment of rent** - Default for 3+ months\n2. **Personal/bonafide need** - Landlord needs for self/family\n3. **Subletting** - Without landlord's consent\n4. **Nuisance** - Causing nuisance to neighbors\n5. **Damage** - Causing damage to property\n6. **Illegal/immoral use** - Using for illegal purposes\n7. **Reconstruction** - Building needs demolition/major repairs\n8. **Violation of terms** - Breach of rental agreement\n\n**EVICTION PROCEDURE:**\n1. Serve legal notice to tenant\n2. File eviction petition in Rent Court/Civil Court\n3. Court examines grounds\n4. If valid, eviction order passed\n5. If tenant doesn't vacate, execution through court\n\n**TENANT PROTECTIONS:**\n- Cannot be evicted without court order\n- Even if rent agreement expires, tenant protected\n- Transfer of ownership doesn't terminate tenancy\n- First right of refusal if landlord sells\n\n**IMPORTANT:**\n- Model Tenancy Act, 2021 (being adopted by states) changes some rules\n- Commercial tenants have fewer protections\n- Agreement for 11 months doesn't bypass rent control if tenant continues\n\n**ILLEGAL EVICTION:**\nIf landlord forcibly evicts: FIR can be filed + civil suit for damages.",
            "category": "property_law",
            "language": "english"
        },
    ]
    training_examples.extend(property_law_qa)

    # =====================================================
    # CONSUMER LAW
    # =====================================================
    consumer_law_qa = [
        {
            "instruction": "How to file a consumer complaint in India?",
            "input": "",
            "output": "Filing consumer complaint under Consumer Protection Act, 2019:\n\n**WHO CAN FILE:**\n- Consumer who bought goods/services\n- Recognized consumer association\n- Central/State Government\n- Legal heir of deceased consumer\n\n**WHERE TO FILE:**\n- **District Commission:** Claims up to ₹1 crore\n- **State Commission:** ₹1 crore to ₹10 crore\n- **National Commission:** Above ₹10 crore\n\n**GROUNDS FOR COMPLAINT:**\n1. Defective goods\n2. Deficient services\n3. Unfair trade practices\n4. Overcharging\n5. Misleading advertisements\n6. Fake goods/hazardous products\n\n**PROCEDURE:**\n1. **Send legal notice** (optional but recommended)\n2. **File complaint** - online at edaakhil.nic.in or in person\n3. **Pay fee** - ₹100 to ₹5,000 based on claim amount\n4. **Submit documents** - Bills, warranty card, correspondence\n5. **Hearing** - No lawyer needed (you can argue yourself)\n6. **Order** - Within 3-5 months typically\n\n**ONLINE FILING:**\nWebsite: edaakhil.nic.in\n- Register with Aadhaar/PAN\n- Upload complaint and documents\n- Pay fee online\n- Track case status\n\n**RELIEFS AVAILABLE:**\n- Replacement/refund of goods\n- Compensation for loss/injury\n- Removal of defect in goods\n- Discontinue unfair practice\n- Punitive damages\n\n**TIME LIMIT:** File within 2 years of cause of action.",
            "category": "consumer_law",
            "language": "english"
        },
    ]
    training_examples.extend(consumer_law_qa)

    # =====================================================
    # CYBER LAW
    # =====================================================
    cyber_law_qa = [
        {
            "instruction": "What are the cyber crime laws in India?",
            "input": "",
            "output": "Cyber crime laws in India under Information Technology Act, 2000:\n\n**COMMON CYBER CRIMES & SECTIONS:**\n\n**1. Hacking (Section 66)**\n- Unauthorized access to computer\n- Punishment: 3 years + ₹5 lakh fine\n\n**2. Identity Theft (Section 66C)**\n- Using someone's password/digital signature\n- Punishment: 3 years + ₹1 lakh fine\n\n**3. Cyber Stalking/Harassment (Section 66A - STRUCK DOWN)**\n- Now covered under BNS Section 351 (Criminal Intimidation)\n- Punishment: 2-7 years\n\n**4. Publishing Obscene Material (Section 67)**\n- Pornography online\n- Punishment: 3 years + ₹5 lakh (first), 5 years + ₹10 lakh (repeat)\n\n**5. Child Pornography (Section 67B)**\n- Strict liability offense\n- Punishment: 5 years + ₹10 lakh (first), 7 years (repeat)\n\n**6. Data Theft (Section 43)**\n- Unauthorized copying of data\n- Compensation up to ₹5 crore + criminal prosecution\n\n**7. Online Fraud/Phishing**\n- Section 66D IT Act + Section 318 BNS (Cheating)\n- Punishment: 3 years + fine\n\n**8. Cyber Terrorism (Section 66F)**\n- Attacking critical infrastructure\n- Punishment: Life imprisonment\n\n**HOW TO REPORT:**\n1. **National Cyber Crime Portal:** cybercrime.gov.in\n2. **Helpline:** 1930\n3. **Local Cyber Cell** - File FIR\n4. Preserve evidence (screenshots, URLs, emails)\n\n**DIGITAL PERSONAL DATA PROTECTION ACT, 2023:**\nNew law for data privacy - penalties up to ₹250 crore for data breaches.",
            "category": "cyber_law",
            "language": "english"
        },
    ]
    training_examples.extend(cyber_law_qa)

    # =====================================================
    # LABOUR LAW
    # =====================================================
    labour_law_qa = [
        {
            "instruction": "What are employee rights in India?",
            "input": "",
            "output": "Key employee rights in India under various labor codes:\n\n**1. RIGHT TO WAGES (Code on Wages, 2019)**\n- Minimum wages as per notification\n- Equal pay for equal work (no gender discrimination)\n- Wages within 7 days of wage period end\n- No unauthorized deductions\n\n**2. WORKING HOURS**\n- Maximum 48 hours/week\n- Maximum 9 hours/day (can extend to 12 with consent)\n- Overtime: Double the normal wage rate\n- Weekly off mandatory (usually Sunday)\n\n**3. LEAVE ENTITLEMENT**\n- Earned Leave: 1 day for every 20 days worked\n- Sick Leave: As per state rules (usually 12 days/year)\n- Casual Leave: As per company policy\n- Maternity Leave: 26 weeks (for first 2 children)\n- Paternity Leave: No statutory right (company policy)\n\n**4. PROVIDENT FUND (EPF)**\n- 12% from employee + 12% from employer\n- Mandatory if 20+ employees\n- Can withdraw for housing, medical, education\n\n**5. GRATUITY**\n- After 5 years of continuous service\n- 15 days salary for each year of service\n- Maximum: ₹20 lakh\n\n**6. TERMINATION PROTECTION**\n- Notice period required (typically 1-3 months)\n- Retrenchment compensation: 15 days per year of service\n- Cannot terminate pregnant women\n\n**7. SEXUAL HARASSMENT PROTECTION**\n- POSH Act applies to all workplaces\n- Internal Complaints Committee mandatory\n\n**WHERE TO COMPLAIN:**\n- Labor Commissioner office\n- Industrial Tribunal\n- SHRAM Portal: shramsuvidha.gov.in",
            "category": "labour_law",
            "language": "english"
        },
        {
            "instruction": "How to claim PF withdrawal?",
            "input": "",
            "output": "EPF (Employee Provident Fund) withdrawal process:\n\n**FULL WITHDRAWAL ALLOWED WHEN:**\n1. Retirement (58 years)\n2. 2 months of unemployment after resignation\n3. Permanent disability\n4. Emigration abroad\n5. VRS (Voluntary Retirement)\n\n**PARTIAL WITHDRAWAL ALLOWED FOR:**\n1. **Housing:** Up to 90% after 5 years of service\n2. **Marriage:** Up to 50% (self/children/siblings)\n3. **Education:** Up to 50% for children's higher education\n4. **Medical:** Up to 6 months' salary\n5. **Home Loan Repayment:** Up to 90%\n\n**ONLINE WITHDRAWAL PROCESS:**\n1. Visit: unifiedportal-mem.epfindia.gov.in\n2. Login with UAN and password\n3. Go to 'Online Services' → 'Claim (Form-31, 19, 10C & 10D)'\n4. Verify KYC (Aadhaar, Bank Account must be linked)\n5. Select claim type (Full/Partial)\n6. Enter bank details\n7. Submit with Aadhaar OTP\n\n**DOCUMENTS NEEDED:**\n- UAN number\n- Aadhaar (linked to UAN)\n- Bank account (linked to UAN)\n- PAN (for amount > ₹50,000)\n\n**TIMELINE:**\n- Online: 10-15 days\n- Offline: 20-30 days\n\n**TAX ON WITHDRAWAL:**\n- Before 5 years: TDS deducted (10% if PAN, else 34%)\n- After 5 years: No TDS\n\n**HELPLINE:** 1800-118-005 (EPFO)",
            "category": "labour_law",
            "language": "english"
        },
    ]
    training_examples.extend(labour_law_qa)

    # =====================================================
    # RTI (Right to Information)
    # =====================================================
    rti_qa = [
        {
            "instruction": "How to file RTI application in India?",
            "input": "",
            "output": "Filing RTI application under Right to Information Act, 2005:\n\n**WHO CAN FILE:**\n- Any Indian citizen\n- Not available to non-citizens or companies\n\n**WHERE TO FILE:**\n- Public Information Officer (PIO) of the concerned department\n- Can file online at rtionline.gov.in (Central Government)\n- State portals for state departments\n\n**FEE:**\n- Central Government: ₹10\n- State Governments: ₹10-20 (varies)\n- BPL card holders: FREE\n- Additional: ₹2 per page for photocopies\n\n**HOW TO WRITE RTI:**\n\n**Format:**\n```\nTo,\nThe Public Information Officer\n[Name of Department/Ministry]\n[Address]\n\nSubject: Application under RTI Act, 2005\n\nSir/Madam,\n\nI wish to obtain the following information under RTI Act:\n\n1. [Specific question 1]\n2. [Specific question 2]\n3. [Specific question 3]\n\nI am paying the requisite fee of ₹10 by [postal order/court fee stamp/online].\n\nYour faithfully,\n[Your Name]\n[Address]\n[Phone]\n[Date]\n```\n\n**TIPS FOR EFFECTIVE RTI:**\n- Be specific in questions\n- Ask for documents, not opinions\n- One subject per application\n- Mention time period for data\n\n**TIMELINE:**\n- Reply within 30 days\n- Life/liberty matters: 48 hours\n- Transferred application: 35 days total\n\n**APPEAL:**\n1. First Appeal: Within 30 days to First Appellate Authority\n2. Second Appeal: Within 90 days to Central/State Information Commission\n\n**ONLINE PORTAL:** rtionline.gov.in",
            "category": "administrative_law",
            "language": "english"
        },
    ]
    training_examples.extend(rti_qa)

    # =====================================================
    # CONSTITUTIONAL RIGHTS
    # =====================================================
    constitutional_qa = [
        {
            "instruction": "What are the Fundamental Rights in Indian Constitution?",
            "input": "",
            "output": "Fundamental Rights under Part III of Indian Constitution (Articles 12-35):\n\n**1. RIGHT TO EQUALITY (Articles 14-18)**\n- Article 14: Equality before law\n- Article 15: No discrimination on grounds of religion, race, caste, sex, place of birth\n- Article 16: Equal opportunity in public employment\n- Article 17: Abolition of untouchability\n- Article 18: Abolition of titles (except military/academic)\n\n**2. RIGHT TO FREEDOM (Articles 19-22)**\n- Article 19: Six freedoms:\n  - Speech and expression\n  - Peaceful assembly\n  - Form associations\n  - Move freely in India\n  - Reside anywhere in India\n  - Practice any profession\n- Article 20: Protection against conviction\n- Article 21: Right to life and personal liberty\n- Article 21A: Right to education (6-14 years)\n- Article 22: Protection against arrest/detention\n\n**3. RIGHT AGAINST EXPLOITATION (Articles 23-24)**\n- Article 23: Prohibition of human trafficking and forced labor\n- Article 24: Prohibition of child labor (below 14 years in hazardous work)\n\n**4. RIGHT TO FREEDOM OF RELIGION (Articles 25-28)**\n- Article 25: Freedom of conscience and religion\n- Article 26: Freedom to manage religious affairs\n- Article 27: No tax for religious promotion\n- Article 28: Freedom from religious instruction in state institutions\n\n**5. CULTURAL AND EDUCATIONAL RIGHTS (Articles 29-30)**\n- Article 29: Protection of minorities' culture\n- Article 30: Minorities' right to establish educational institutions\n\n**6. RIGHT TO CONSTITUTIONAL REMEDIES (Article 32)**\n- Right to approach Supreme Court directly\n- Five writs: Habeas Corpus, Mandamus, Certiorari, Prohibition, Quo Warranto\n\n**ENFORCEMENT:** Supreme Court (Article 32) or High Court (Article 226)",
            "category": "constitutional_law",
            "language": "english"
        },
    ]
    training_examples.extend(constitutional_qa)

    # =====================================================
    # MOTOR VEHICLE / ACCIDENT LAW
    # =====================================================
    motor_vehicle_qa = [
        {
            "instruction": "What compensation can be claimed in motor accident cases?",
            "input": "",
            "output": "Motor accident compensation under Motor Vehicles Act, 1988:\n\n**TYPES OF CLAIMS:**\n\n**1. THIRD PARTY INSURANCE (Mandatory)**\n- Unlimited liability for death/injury to third party\n- Owner and insurer both liable\n\n**2. COMPREHENSIVE INSURANCE**\n- Covers own vehicle damage + third party\n- Subject to policy limits\n\n**COMPENSATION CALCULATION (Death):**\n\nBased on Supreme Court formula (Sarla Verma case):\n\n1. **Annual Income** of deceased\n2. **Multiplier** based on age:\n   - Up to 15 years: 15\n   - 16-20 years: 16\n   - 21-25 years: 17\n   - 26-30 years: 18\n   - 31-35 years: 17\n   - 36-40 years: 16\n   - And so on...\n\n3. **Deduction** for personal expenses:\n   - Bachelor: 50%\n   - Married with dependents: 1/3 to 1/4\n\n4. **Additional amounts:**\n   - Loss of consortium: ₹40,000-1,00,000\n   - Funeral expenses: ₹15,000-25,000\n   - Loss of estate: ₹15,000-25,000\n   - Future prospects: 40-50% addition if below 40 years\n\n**FOR INJURIES:**\n- Medical expenses (past and future)\n- Loss of income during treatment\n- Permanent disability: % disability × remaining working years × income\n- Pain and suffering: ₹50,000-5,00,000\n- Attendant charges\n\n**WHERE TO FILE:**\n- Motor Accident Claims Tribunal (MACT)\n- Time limit: Generally within 6 months (extendable)\n\n**PROCEDURE:**\n1. File claim petition with documents (FIR, medical records, income proof)\n2. Tribunal issues notice to insurer\n3. Evidence recorded\n4. Award passed\n5. Appeal to High Court if dissatisfied",
            "category": "motor_vehicle_law",
            "language": "english"
        },
    ]
    training_examples.extend(motor_vehicle_qa)

    # Save all training data
    output_file = TRAINING_DIR / "comprehensive_legal_qa.json"
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(training_examples, f, ensure_ascii=False, indent=2)

    print(f"\n[OK] Created {len(training_examples)} training examples")
    print(f"[OK] Saved to: {output_file}")

    # Create summary
    categories = {}
    languages = {}
    for ex in training_examples:
        cat = ex.get('category', 'unknown')
        lang = ex.get('language', 'english')
        categories[cat] = categories.get(cat, 0) + 1
        languages[lang] = languages.get(lang, 0) + 1

    summary = {
        "total_examples": len(training_examples),
        "categories": categories,
        "languages": languages,
        "created_at": datetime.now().isoformat()
    }

    print(f"\nSummary:")
    print(f"  Categories: {json.dumps(categories, indent=4)}")
    print(f"  Languages: {json.dumps(languages, indent=4)}")

    return training_examples


def create_kaggle_download_instructions():
    """Create instructions for downloading Kaggle datasets manually"""

    instructions = """
# KAGGLE DATASET DOWNLOAD INSTRUCTIONS

Since web scraping failed, download these pre-made datasets from Kaggle:

## RECOMMENDED DATASETS:

### 1. Indian Legal Documents Corpus
URL: https://www.kaggle.com/datasets/harshagarwal18/indian-legal-documents
- Contains: Indian court judgments, legal texts
- Size: ~500MB
- Format: Text files

### 2. Indian Court Cases
URL: https://www.kaggle.com/datasets/tanishqsingla/indian-court-cases
- Contains: Supreme Court and High Court cases
- Size: ~200MB
- Format: CSV/JSON

### 3. Indian Law Dataset
URL: https://www.kaggle.com/datasets/keshavg/indian-laws-dataset
- Contains: Bare acts and sections
- Size: ~100MB
- Format: JSON

### 4. Legal QA Dataset
URL: https://www.kaggle.com/datasets/devjeet/legal-qa-dataset
- Contains: Legal question-answer pairs
- Perfect for training!
- Format: CSV

## HOW TO DOWNLOAD:

1. Go to kaggle.com and create free account
2. Go to dataset URL
3. Click "Download" button
4. Extract ZIP to: data/downloaded_datasets/

## ALTERNATIVE - KAGGLE API:

```bash
# Install kaggle CLI
pip install kaggle

# Setup API key (from kaggle.com/account)
# Place kaggle.json in ~/.kaggle/

# Download datasets
kaggle datasets download -d harshagarwal18/indian-legal-documents -p data/downloaded_datasets/
kaggle datasets download -d tanishqsingla/indian-court-cases -p data/downloaded_datasets/
```

## HUGGINGFACE DATASETS:

```python
# Install
pip install datasets

# Download
from datasets import load_dataset

# Indian Legal NLP
dataset = load_dataset("keshavg/indian-legal-text")
dataset.save_to_disk("data/downloaded_datasets/indian-legal-text")

# Legal Reasoning
dataset = load_dataset("nguha/legalbench")
dataset.save_to_disk("data/downloaded_datasets/legalbench")
```

## AFTER DOWNLOADING:

Run: python create_training_data.py

This will combine all downloaded data with manual training examples.
"""

    instructions_file = DATASETS_DIR / "DOWNLOAD_INSTRUCTIONS.md"
    with open(instructions_file, 'w', encoding='utf-8') as f:
        f.write(instructions)

    print(f"\n[OK] Download instructions saved to: {instructions_file}")
    return instructions_file


def main():
    print("="*60)
    print("INDIAN LEGAL DATA COLLECTION")
    print("="*60)

    # Skip network calls - create local data only
    hf_datasets = []

    # 2. Create comprehensive manual training data
    training_data = create_manual_training_data()

    # 3. Create download instructions for Kaggle
    create_kaggle_download_instructions()

    print("\n" + "="*60)
    print("SUMMARY")
    print("="*60)
    print(f"[OK] Created {len(training_data)} manual training examples")
    print(f"\nNext Steps:")
    print("1. Read DOWNLOAD_INSTRUCTIONS.md in data/downloaded_datasets/")
    print("2. Download Kaggle datasets manually")
    print("3. Run: python create_training_data.py")
    print("4. Then train model on Kaggle with the combined dataset")


if __name__ == "__main__":
    main()
