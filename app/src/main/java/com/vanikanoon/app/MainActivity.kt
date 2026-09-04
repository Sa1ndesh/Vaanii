package com.vanikanoon.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vanikanoon.app.data.auth.UserManager
import com.vanikanoon.app.data.repository.LegalRepository
import com.vanikanoon.app.ui.components.VaniTopAppBar
import com.vanikanoon.app.ui.screens.advocate.AdvocatePortalScreen
import com.vanikanoon.app.ui.screens.analyzer.FirAnalyzerScreen
import com.vanikanoon.app.ui.screens.auth.ForgotPasswordScreen
import com.vanikanoon.app.ui.screens.auth.LoginScreen
import com.vanikanoon.app.ui.screens.auth.ProfileScreen
import com.vanikanoon.app.ui.screens.auth.SignUpScreen
import com.vanikanoon.app.ui.screens.chat.ChatbotScreen
import com.vanikanoon.app.ui.screens.docs.DocumentGeneratorScreen
import com.vanikanoon.app.ui.screens.faq.FaqBuilderScreen
import com.vanikanoon.app.ui.screens.home.HomeScreen
import com.vanikanoon.app.ui.screens.knowledge.KnowledgeBaseScreen
import com.vanikanoon.app.ui.screens.learning.LearningHubScreen
import com.vanikanoon.app.ui.screens.notices.NoticeGeneratorScreen
import com.vanikanoon.app.ui.screens.settings.SettingsScreen
import com.vanikanoon.app.ui.screens.student.StudentPortalScreen
import com.vanikanoon.app.ui.screens.summarizer.CaseSummarizerScreen
import com.vanikanoon.app.ui.screens.support.AboutScreen
import com.vanikanoon.app.ui.screens.support.CustomerSupportScreen
import com.vanikanoon.app.ui.screens.support.PrivacyPolicyScreen
import com.vanikanoon.app.ui.screens.support.ReportIssueScreen
import com.vanikanoon.app.ui.screens.support.TermsScreen
import com.vanikanoon.app.ui.screens.vani.VaniVoiceScreen
import com.vanikanoon.app.ui.theme.LegalDeepBlue
import com.vanikanoon.app.ui.theme.LegalGoldPrimary
import com.vanikanoon.app.ui.theme.LegalSurfaceWhite
import com.vanikanoon.app.ui.theme.VaniKanoonTheme

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Home : Screen("home", "Vani-Kanoon", Icons.Default.Home)
    object VaniVoice : Screen("vani_voice", "Voice Assistant", Icons.Default.Mic)
    object Chatbot : Screen("chatbot", "Legal Chat AI", Icons.Default.Chat)
    object AdvocatePortal : Screen("advocate_portal", "Advocate Portal", Icons.Default.Balance)
    object StudentPortal : Screen("student_portal", "Student Hub", Icons.Default.School)

    // Specific Module Screens
    object Documents : Screen("documents", "Document Generator", Icons.Default.Description)
    object Notices : Screen("notices", "Statutory Notices")
    object Analyzer : Screen("analyzer", "FIR & Evidence Analyzer")
    object Summarizer : Screen("summarizer", "Case Law Summarizer")
    object Learning : Screen("learning", "Student & Citizen Hub")
    object Faq : Screen("faq", "Client FAQ Builder")
    object KnowledgeBase : Screen("knowledge_base", "Bare Acts & Rights", Icons.Default.Assignment)
    object Settings : Screen("settings", "Settings & About")

    // Auth Screens
    object Login : Screen("login", "Sign In")
    object SignUp : Screen("signup", "Create Account")
    object ForgotPassword : Screen("forgot_password", "Reset Password")
    object Profile : Screen("profile", "User Profile", Icons.Default.AccountCircle)

    // Support & Info Screens
    object CustomerSupport : Screen("customer_support", "Customer Support")
    object ReportIssue : Screen("report_issue", "Report an Issue")
    object Terms : Screen("terms", "Terms & Conditions")
    object PrivacyPolicy : Screen("privacy_policy", "Privacy Policy")
    object About : Screen("about", "About Vani-Kanoon")
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.VaniVoice,
    Screen.Chatbot,
    Screen.AdvocatePortal,
    Screen.StudentPortal
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VaniKanoonTheme {
                VaniKanoonApp()
            }
        }
    }
}

@Composable
fun VaniKanoonApp() {
    val context = LocalContext.current
    val repository = remember { LegalRepository(context) }
    val userManager = remember { UserManager(context) }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    val currentScreen = when (currentRoute) {
        Screen.Home.route -> Screen.Home
        Screen.VaniVoice.route -> Screen.VaniVoice
        Screen.Chatbot.route -> Screen.Chatbot
        Screen.AdvocatePortal.route -> Screen.AdvocatePortal
        Screen.StudentPortal.route -> Screen.StudentPortal
        Screen.Documents.route -> Screen.Documents
        Screen.Notices.route -> Screen.Notices
        Screen.Analyzer.route -> Screen.Analyzer
        Screen.Summarizer.route -> Screen.Summarizer
        Screen.Learning.route -> Screen.Learning
        Screen.Faq.route -> Screen.Faq
        Screen.KnowledgeBase.route -> Screen.KnowledgeBase
        Screen.Settings.route -> Screen.Settings
        Screen.Login.route -> Screen.Login
        Screen.SignUp.route -> Screen.SignUp
        Screen.ForgotPassword.route -> Screen.ForgotPassword
        Screen.Profile.route -> Screen.Profile
        Screen.CustomerSupport.route -> Screen.CustomerSupport
        Screen.ReportIssue.route -> Screen.ReportIssue
        Screen.Terms.route -> Screen.Terms
        Screen.PrivacyPolicy.route -> Screen.PrivacyPolicy
        Screen.About.route -> Screen.About
        else -> Screen.Home
    }

    val isTopLevelScreen = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        topBar = {
            VaniTopAppBar(
                title = currentScreen.title,
                subtitle = if (currentScreen == Screen.Home) "AI Legal Assistant • Indian Law" else null,
                onSettingsClick = {
                    if (currentRoute != Screen.Settings.route) {
                        navController.navigate(Screen.Settings.route)
                    }
                },
                navigationIcon = if (!isTopLevelScreen) {
                    {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = LegalSurfaceWhite
                            )
                        }
                    }
                } else null
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = LegalDeepBlue,
                contentColor = LegalSurfaceWhite,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                bottomNavItems.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            screen.icon?.let {
                                Icon(
                                    imageVector = it,
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = when (screen) {
                                    Screen.Home -> "Home"
                                    Screen.VaniVoice -> "Voice"
                                    Screen.Chatbot -> "Chat"
                                    Screen.AdvocatePortal -> "Advocate"
                                    Screen.StudentPortal -> "Student"
                                    else -> screen.title
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LegalDeepBlue,
                            unselectedIconColor = LegalSurfaceWhite.copy(alpha = 0.65f),
                            selectedTextColor = LegalGoldPrimary,
                            unselectedTextColor = LegalSurfaceWhite.copy(alpha = 0.65f),
                            indicatorColor = LegalGoldPrimary
                        ),
                        modifier = Modifier.testTag("nav_tab_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        userManager = userManager,
                        onNavigateToVaniVoice = { navController.navigate(Screen.VaniVoice.route) },
                        onNavigateToChatbot = { navController.navigate(Screen.Chatbot.route) },
                        onNavigateToAdvocatePortal = { navController.navigate(Screen.AdvocatePortal.route) },
                        onNavigateToStudentPortal = { navController.navigate(Screen.StudentPortal.route) },
                        onNavigateToDocuments = { navController.navigate(Screen.Documents.route) },
                        onNavigateToNotices = { navController.navigate(Screen.Notices.route) },
                        onNavigateToAnalyzer = { navController.navigate(Screen.Analyzer.route) },
                        onNavigateToSummarizer = { navController.navigate(Screen.Summarizer.route) },
                        onNavigateToLearning = { navController.navigate(Screen.Learning.route) },
                        onNavigateToFaq = { navController.navigate(Screen.Faq.route) },
                        onNavigateToKnowledgeBase = { navController.navigate(Screen.KnowledgeBase.route) },
                        onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                        onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                        onNavigateToCustomerSupport = { navController.navigate(Screen.CustomerSupport.route) },
                        onNavigateToAbout = { navController.navigate(Screen.About.route) },
                        onNavigateToTerms = { navController.navigate(Screen.Terms.route) }
                    )
                }

                composable(Screen.VaniVoice.route) {
                    VaniVoiceScreen(
                        repository = repository,
                        userManager = userManager,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Chatbot.route) {
                    ChatbotScreen(
                        repository = repository,
                        userManager = userManager
                    )
                }

                composable(Screen.AdvocatePortal.route) {
                    AdvocatePortalScreen(
                        userManager = userManager,
                        onNavigateToDocuments = { navController.navigate(Screen.Documents.route) },
                        onNavigateToNotices = { navController.navigate(Screen.Notices.route) },
                        onNavigateToAnalyzer = { navController.navigate(Screen.Analyzer.route) },
                        onNavigateToSummarizer = { navController.navigate(Screen.Summarizer.route) },
                        onNavigateToFaq = { navController.navigate(Screen.Faq.route) },
                        onNavigateToKnowledgeBase = { navController.navigate(Screen.KnowledgeBase.route) }
                    )
                }

                composable(Screen.StudentPortal.route) {
                    StudentPortalScreen(
                        userManager = userManager,
                        onNavigateToLearning = { navController.navigate(Screen.Learning.route) },
                        onNavigateToSummarizer = { navController.navigate(Screen.Summarizer.route) },
                        onNavigateToKnowledgeBase = { navController.navigate(Screen.KnowledgeBase.route) },
                        onNavigateToFaq = { navController.navigate(Screen.Faq.route) }
                    )
                }

                composable(Screen.Documents.route) {
                    DocumentGeneratorScreen(repository = repository)
                }

                composable(Screen.Notices.route) {
                    NoticeGeneratorScreen(repository = repository)
                }

                composable(Screen.Analyzer.route) {
                    FirAnalyzerScreen(repository = repository)
                }

                composable(Screen.Summarizer.route) {
                    CaseSummarizerScreen(repository = repository)
                }

                composable(Screen.Learning.route) {
                    LearningHubScreen(repository = repository)
                }

                composable(Screen.Faq.route) {
                    FaqBuilderScreen(repository = repository)
                }

                composable(Screen.KnowledgeBase.route) {
                    KnowledgeBaseScreen()
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        userManager = userManager,
                        onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                        onNavigateToCustomerSupport = { navController.navigate(Screen.CustomerSupport.route) },
                        onNavigateToReportIssue = { navController.navigate(Screen.ReportIssue.route) },
                        onNavigateToTerms = { navController.navigate(Screen.Terms.route) },
                        onNavigateToPrivacyPolicy = { navController.navigate(Screen.PrivacyPolicy.route) },
                        onNavigateToAbout = { navController.navigate(Screen.About.route) }
                    )
                }

                // Auth Routes
                composable(Screen.Login.route) {
                    LoginScreen(
                        userManager = userManager,
                        onLoginSuccess = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } },
                        onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                        onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                        onContinueAsGuest = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } }
                    )
                }

                composable(Screen.SignUp.route) {
                    SignUpScreen(
                        userManager = userManager,
                        onSignUpSuccess = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } },
                        onNavigateToLogin = { navController.popBackStack() }
                    )
                }

                composable(Screen.ForgotPassword.route) {
                    ForgotPasswordScreen(
                        onBackToLogin = { navController.popBackStack() }
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        userManager = userManager,
                        onLogout = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } }
                    )
                }

                // Support & Info Routes
                composable(Screen.CustomerSupport.route) {
                    CustomerSupportScreen()
                }

                composable(Screen.ReportIssue.route) {
                    ReportIssueScreen()
                }

                composable(Screen.Terms.route) {
                    TermsScreen()
                }

                composable(Screen.PrivacyPolicy.route) {
                    PrivacyPolicyScreen()
                }

                composable(Screen.About.route) {
                    AboutScreen()
                }
            }
        }
    }
}
