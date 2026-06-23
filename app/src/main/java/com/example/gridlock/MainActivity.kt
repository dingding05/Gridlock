package com.example.gridlock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

// Theme Colors
val BgColor = Color(0xFF0B131E)
val CardColor = Color(0xFF0E1218)
val TabColor = Color(0xFF141820)
val BorderColor = Color(0xFF1C2230)
val CyanAccent = Color(0xFF00D4FF)
val GreenAccent = Color(0xFF00E676)
val RedAccent = Color(0xFFFF6240)
val YellowAccent = Color(0xFFFFD000)
val TextPrimary = Color(0xFFE2E8F4)
val TextSecondary = Color(0xFF8899AA)
val TextTertiary = Color(0xFF3A4A5C)

// Models
data class Car(val plate: String, val make: String, val model: String, val color: String)

data class User(
    val firstName: String,
    val lastName: String,
    val phone: String,
    val cars: List<Car>,
    var balance: Double
)

data class Spot(
    val id: String,
    val num: Int,
    val row: Int,
    val col: Int,
    var status: String, // "free", "taken", "mine", "lift"
    val isLift: Boolean = false,
    val isRec: Boolean = false
)

enum class Tab { MAP, TICKET, FIND, REPORT, WALLET, PROFILE }
enum class Lot { UNIV, TRAM }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GridlockTheme {
                GridlockApp()
            }
        }
    }
}

@Composable
fun GridlockTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = BgColor,
            surface = CardColor,
            primary = CyanAccent,
            onSurface = TextPrimary
        ),
        content = content
    )
}

@Composable
fun GridlockApp() {
    var user by remember { mutableStateOf<User?>(null) }
    var currentTab by remember { mutableStateOf(Tab.MAP) }
    var isInsideFacility by remember { mutableStateOf(false) }
    var currentLot by remember { mutableStateOf(Lot.UNIV) }
    var currentLevel by remember { mutableIntStateOf(0) }
    var selectedSpotId by remember { mutableStateOf<String?>(null) }
    var mySpot by remember { mutableStateOf<Spot?>(null) }
    var hasTicket by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var selectedDuration by remember { mutableStateOf<String?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Generate spots
    val uSpots = remember { 
        List(3) { lvl -> 
            List(100) { i ->
                val row = i / 10
                val col = i % 10
                val id = (lvl * 100 + i + 1).toString().padStart(3, '0')
                val isLift = row == 4 && col == 4
                val isRec = (row == 4 && col == 5) || (row == 4 && col == 3) || (row == 3 && col == 4)
                val status = if (isLift) "lift" else if ((id.hashCode() % 10) < 5) "taken" else "free"
                Spot(id, i + 1, row, col, status, isLift, isRec)
            }
        }
    }
    
    val tSpots = remember {
        List(400) { i ->
            val row = i / 20
            val col = i % 20
            val id = "T${(i + 1).toString().padStart(3, '0')}"
            val isRec = i == 41 || i == 42
            val status = if ((id.hashCode() % 10) < 4) "taken" else "free"
            Spot(id, i + 1, row, col, status, isRec = isRec)
        }
    }

    LaunchedEffect(hasTicket) {
        if (hasTicket) {
            while (true) {
                delay(1000)
                elapsedSeconds++
            }
        } else {
            elapsedSeconds = 0
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (user != null) {
                BottomNav(currentTab, onTabSelect = { currentTab = it; selectedSpotId = null })
            }
        },
        containerColor = BgColor
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (user == null) {
                AuthScreen(onLogin = {
                    user = User(
                        "Ahmed", "Al Mansouri", "+971 50 123 4567",
                        listOf(Car("AUH·K42831", "Toyota", "Camry", "Silver")),
                        47.50
                    )
                })
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopBar(user!!, isInsideFacility, onToggleLpr = { isInsideFacility = !isInsideFacility })
                    
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp)) {
                        when (currentTab) {
                            Tab.MAP -> MapTab(
                                currentLot, currentLevel, uSpots, tSpots, selectedSpotId, isInsideFacility, hasTicket, selectedDuration,
                                onLotChange = { currentLot = it; selectedSpotId = null },
                                onLevelChange = { currentLevel = it; selectedSpotId = null },
                                onSpotSelect = { selectedSpotId = it },
                                onDurationSelect = { selectedDuration = it },
                                onConfirm = {
                                    hasTicket = true
                                    val spot = if (currentLot == Lot.UNIV) uSpots[currentLevel].find { it.id == selectedSpotId } else tSpots.find { it.id == selectedSpotId }
                                    mySpot = spot?.copy(status = "mine")
                                    currentTab = Tab.TICKET
                                }
                            )
                            Tab.TICKET -> TicketTab(hasTicket, mySpot, user!!, selectedDuration, elapsedSeconds, onExit = {
                                hasTicket = false
                                mySpot = null
                                selectedSpotId = null
                                isInsideFacility = false
                                currentTab = Tab.MAP
                                user = user!!.copy(balance = user!!.balance - 10.0)
                            })
                            Tab.FIND -> FindCarTab(mySpot)
                            Tab.REPORT -> ReportTab(onReported = {
                                scope.launch { snackbarHostState.showSnackbar("Report successfully submitted!") }
                            })
                            Tab.WALLET -> WalletTab(user!!, onTopUp = { amt -> user = user!!.copy(balance = user!!.balance + amt) })
                            Tab.PROFILE -> ProfileTab(user!!, onUpdateUser = { user = it }, onLogout = { user = null; hasTicket = false })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuthScreen(onLogin: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("GRIDLOCK", color = CyanAccent, fontSize = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
        Text("University Smart Parking · AUH", color = TextSecondary, fontSize = 14.sp)
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onLogin,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Quick Demo Login →", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TopBar(user: User, isInside: Boolean, onToggleLpr: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("GRIDLOCK", color = CyanAccent, fontSize = 20.sp, fontWeight = FontWeight.Medium, letterSpacing = 3.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = CardColor, border = BorderStroke(1.dp, BorderColor), shape = RoundedCornerShape(4.dp)) {
                    Text(user.cars.firstOrNull()?.plate ?: "NO PLATE", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = TextPrimary, fontSize = 11.sp)
                }
                Spacer(Modifier.width(8.dp))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (isInside) GreenAccent else RedAccent))
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("UNIVERSITY PARKING ", color = TextSecondary, fontSize = 11.sp, letterSpacing = 1.sp)
            Spacer(Modifier.width(8.dp))
            Surface(
                onClick = onToggleLpr,
                color = if (isInside) GreenAccent.copy(alpha = 0.1f) else RedAccent.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, if (isInside) GreenAccent.copy(alpha = 0.3f) else RedAccent.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(3.dp)
            ) {
                Text(
                    if (isInside) "● INSIDE" else "○ OUTSIDE",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    color = if (isInside) GreenAccent else RedAccent,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
fun BottomNav(selectedTab: Tab, onTabSelect: (Tab) -> Unit) {
    Surface(
        color = BgColor,
        modifier = Modifier.drawBehind {
            drawLine(BorderColor, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx())
        }
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp, top = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            NavButton("MAP", Icons.Default.Map, selectedTab == Tab.MAP) { onTabSelect(Tab.MAP) }
            NavButton("TICKET", Icons.Default.ConfirmationNumber, selectedTab == Tab.TICKET) { onTabSelect(Tab.TICKET) }
            NavButton("FIND", Icons.Default.Search, selectedTab == Tab.FIND) { onTabSelect(Tab.FIND) }
            NavButton("REPORT", Icons.Default.Report, selectedTab == Tab.REPORT) { onTabSelect(Tab.REPORT) }
            NavButton("WALLET", Icons.Default.AccountBalanceWallet, selectedTab == Tab.WALLET) { onTabSelect(Tab.WALLET) }
            NavButton("PROFILE", Icons.Default.Person, selectedTab == Tab.PROFILE) { onTabSelect(Tab.PROFILE) }
        }
    }
}

@Composable
fun NavButton(label: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(60.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .background(if (isSelected) CyanAccent.copy(alpha = 0.12f) else Color.Transparent)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = if (isSelected) CyanAccent else TextSecondary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, color = if (isSelected) CyanAccent else TextSecondary, fontSize = 8.sp)
    }
}

@Composable
fun MapTab(
    lot: Lot, level: Int, uSpots: List<List<Spot>>, tSpots: List<Spot>,
    selectedId: String?, isInside: Boolean, hasTicket: Boolean, selectedDuration: String?,
    onLotChange: (Lot) -> Unit, onLevelChange: (Int) -> Unit, onSpotSelect: (String) -> Unit,
    onDurationSelect: (String) -> Unit, onConfirm: () -> Unit
) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        if (!isInside && !hasTicket) {
            Surface(color = CardColor, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, BorderColor)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🔒", fontSize = 20.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Outside Facility", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Spot booking is only available after LPR confirms entry.", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        Surface(color = CardColor, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, BorderColor)) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("SELECT LOT", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(11.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LotTab("University", "3 levels", lot == Lot.UNIV) { onLotChange(Lot.UNIV) }
                    LotTab("Tram Stop", "Open air", lot == Lot.TRAM) { onLotChange(Lot.TRAM) }
                }
                Spacer(Modifier.height(12.dp))
                
                if (lot == Lot.UNIV) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("University Basement", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Text("LPR · Covered · 3 levels", color = TextSecondary, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(uSpots[level].count { it.status == "free" }.toString(), color = GreenAccent, fontSize = 26.sp, fontWeight = FontWeight.Medium)
                            Text("/ 100 FREE", color = TextSecondary, fontSize = 9.sp)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (0..2).forEach { l ->
                            LevelTab("B${l + 1}", level == l) { onLevelChange(l) }
                        }
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Tram Stop Parking", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Text("Open air · LPR exit gate", color = TextSecondary, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(tSpots.count { it.status == "free" }.toString(), color = GreenAccent, fontSize = 26.sp, fontWeight = FontWeight.Medium)
                            Text("/ 400 FREE", color = TextSecondary, fontSize = 9.sp)
                        }
                    }
                }
                
                Spacer(Modifier.height(12.dp))
                ParkingGrid(if (lot == Lot.UNIV) uSpots[level] else tSpots, selectedId, lot == Lot.UNIV, onSpotSelect)
            }
        }
        
        if (selectedId != null && !hasTicket && isInside) {
            Spacer(Modifier.height(12.dp))
            Surface(color = CardColor, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, BorderColor)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("CLAIM SPOT $selectedId", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(maxItemsInEachRow = 5, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("1", "2", "3", "4", "5").forEach { h ->
                            DurationBtn(h, selectedDuration == h) { onDurationSelect(h) }
                        }
                    }
                    if (selectedDuration != null) {
                        val discount = when (selectedDuration) {
                            "1" -> "30%"
                            "2" -> "20%"
                            "3" -> "15%"
                            "4" -> "10%"
                            else -> "0%"
                        }
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            color = GreenAccent.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, GreenAccent.copy(alpha = 0.2f))
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(discount, color = GreenAccent, fontSize = 24.sp, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Early Exit Discount", color = GreenAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("Get this discount if you leave before time", color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onConfirm,
                        enabled = selectedDuration != null,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        shape = RoundedCornerShape(11.dp)
                    ) {
                        Text("◈ Confirm Parking", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(Modifier.height(100.dp))
    }
}

@Composable
fun LotTab(name: String, sub: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.width(100.dp),
        color = if (isSelected) CyanAccent.copy(alpha = 0.12f) else TabColor,
        border = BorderStroke(1.dp, if (isSelected) CyanAccent else BorderColor),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(name, color = if (isSelected) CyanAccent else TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(sub, color = TextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
fun LevelTab(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) CyanAccent.copy(alpha = 0.12f) else Color.Transparent,
        border = BorderStroke(1.dp, if (isSelected) CyanAccent else BorderColor),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(name, modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp), color = if (isSelected) CyanAccent else TextSecondary, fontSize = 10.sp)
    }
}

@Composable
fun DurationBtn(h: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.width(70.dp),
        color = if (isSelected) CyanAccent.copy(alpha = 0.12f) else TabColor,
        border = BorderStroke(1.dp, if (isSelected) CyanAccent else BorderColor),
        shape = RoundedCornerShape(9.dp)
    ) {
        Text("${h}h", modifier = Modifier.padding(vertical = 12.dp), color = if (isSelected) CyanAccent else TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ParkingGrid(spots: List<Spot>, selectedId: String?, isUniv: Boolean, onSpotSelect: (String) -> Unit) {
    if (isUniv) {
        Column(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            repeat(10) { r ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(('A' + r).toString(), color = TextTertiary, fontSize = 8.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                    val rowSpots = spots.filter { it.row == r }
                    rowSpots.take(5).forEach { s -> SpotItem(s, selectedId == s.id) { onSpotSelect(s.id) } }
                    Box(modifier = Modifier.width(10.dp).height(18.dp).background(BorderColor.copy(alpha = 0.3f)))
                    rowSpots.drop(5).forEach { s -> SpotItem(s, selectedId == s.id) { onSpotSelect(s.id) } }
                }
                Spacer(Modifier.height(3.dp))
            }
        }
    } else {
        LazyVerticalGrid(columns = GridCells.Fixed(20), modifier = Modifier.height(300.dp), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            items(spots) { s ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1.4f)
                        .clip(RoundedCornerShape(2.dp))
                        .background(getSpotColor(s.status))
                        .border(1.dp, if (selectedId == s.id) GreenAccent else if (s.isRec) GreenAccent else Color.Transparent)
                        .clickable(enabled = s.status == "free") { onSpotSelect(s.id) }
                )
            }
        }
    }
}

@Composable
fun SpotItem(s: Spot, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 1.5.dp)
            .size(30.dp, 20.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(getSpotColor(s.status))
            .border(
                if (isSelected) 2.dp else 1.dp,
                if (isSelected) GreenAccent else if (s.status == "mine") CyanAccent else if (s.isRec) GreenAccent else Color.Transparent
            )
            .clickable(enabled = s.status == "free") { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            if (s.isLift) "▲" else if (s.status == "mine") "◈" else s.id.takeLast(2),
            color = getSpotTextColor(s.status),
            fontSize = if (s.isLift) 9.sp else 6.5.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

fun getSpotColor(status: String) = when (status) {
    "free" -> GreenAccent.copy(alpha = 0.13f)
    "taken" -> RedAccent.copy(alpha = 0.1f)
    "mine" -> CyanAccent.copy(alpha = 0.2f)
    "lift" -> YellowAccent.copy(alpha = 0.12f)
    else -> Color.Transparent
}

fun getSpotTextColor(status: String) = when (status) {
    "free" -> GreenAccent
    "taken" -> RedAccent.copy(alpha = 0.3f)
    "mine" -> CyanAccent
    "lift" -> YellowAccent
    else -> TextSecondary
}

@Composable
fun TicketTab(hasTicket: Boolean, mySpot: Spot?, user: User, duration: String?, elapsed: Int, onExit: () -> Unit) {
    if (!hasTicket || mySpot == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("NO ACTIVE TICKET", color = TextSecondary, fontSize = 11.sp, letterSpacing = 2.sp)
        }
    } else {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Surface(color = CardColor, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, BorderColor)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("ACTIVE TICKET", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    TicketCard(mySpot, user, duration)
                    Spacer(Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(Color.White, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        RealLookQrCode(mySpot.id)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Surface(color = CardColor, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, BorderColor)) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(formatTime(elapsed), color = YellowAccent, fontSize = 34.sp, fontWeight = FontWeight.Medium, letterSpacing = 3.sp)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onExit,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                        shape = RoundedCornerShape(11.dp)
                    ) {
                        Text("Exit & Pay", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RealLookQrCode(id: String) {
    Canvas(modifier = Modifier.size(120.dp)) {
        val size = 120.dp.toPx()
        val cells = 21
        val cellSize = size / cells
        
        // Background
        drawRect(Color.White, size = Size(size, size))
        
        // Finder patterns (the squares in the corners)
        fun drawFinder(x: Float, y: Float) {
            drawRect(Color.Black, Offset(x, y), Size(cellSize * 7, cellSize * 7))
            drawRect(Color.White, Offset(x + cellSize, y + cellSize), Size(cellSize * 5, cellSize * 5))
            drawRect(Color.Black, Offset(x + cellSize * 2, y + cellSize * 2), Size(cellSize * 3, cellSize * 3))
        }
        
        drawFinder(0f, 0f)
        drawFinder((cells - 7) * cellSize, 0f)
        drawFinder(0f, (cells - 7) * cellSize)
        
        // Random bits (seeded)
        val rand = java.util.Random(id.hashCode().toLong())
        for (r in 0 until cells) {
            for (c in 0 until cells) {
                // Skip finder patterns
                if ((r < 8 && c < 8) || (r < 8 && c > cells - 9) || (r > cells - 9 && c < 8)) continue
                
                if (rand.nextBoolean()) {
                    drawRect(Color.Black, Offset(c * cellSize, r * cellSize), Size(cellSize, cellSize))
                }
            }
        }
    }
}

@Composable
fun TicketCard(spot: Spot, user: User, duration: String?) {
    Column(modifier = Modifier.fillMaxWidth().background(TabColor, RoundedCornerShape(12.dp)).border(1.dp, BorderColor, RoundedCornerShape(12.dp))) {
        Row(modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF0A1624), Color(0xFF0D1E30)))).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(spot.id, color = CyanAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Surface(color = GreenAccent.copy(alpha = 0.12f), border = BorderStroke(1.dp, GreenAccent.copy(alpha = 0.3f)), shape = RoundedCornerShape(5.dp)) {
                Text("✓ VALID", modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp), color = GreenAccent, fontSize = 9.sp)
            }
        }
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            TicketRow("PLATE", user.cars.firstOrNull()?.plate ?: "N/A", CyanAccent)
            TicketRow("SPOT", spot.id, CyanAccent)
            TicketRow("DURATION", "${duration}h", TextPrimary)
        }
    }
}

@Composable
fun TicketRow(lbl: String, valStr: String, valColor: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp).drawBehind {
        drawLine(BorderColor, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
    }, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(lbl, color = TextSecondary, fontSize = 9.sp)
        Text(valStr, color = valColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun FindCarTab(mySpot: Spot?) {
    var selectedElevator by remember { mutableStateOf("Elevator 1") }
    
    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ElevatorBtn("Elevator 1", selectedElevator == "Elevator 1", Modifier.weight(1f)) { selectedElevator = "Elevator 1" }
            ElevatorBtn("Elevator 2", selectedElevator == "Elevator 2", Modifier.weight(1f)) { selectedElevator = "Elevator 2" }
        }
        
        Surface(color = CardColor, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, BorderColor)) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("FIND MY VEHICLE", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(11.dp))
                Box(modifier = Modifier.fillMaxWidth().height(250.dp).background(TabColor, RoundedCornerShape(12.dp)).border(1.dp, BorderColor, RoundedCornerShape(12.dp))) {
                    // Map/Grid view
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val step = 30.dp.toPx()
                        for (x in 0..(size.width / step).toInt()) {
                            drawLine(CyanAccent.copy(alpha = 0.1f), Offset(x * step, 0f), Offset(x * step, size.height), 1f)
                        }
                        for (y in 0..(size.height / step).toInt()) {
                            drawLine(CyanAccent.copy(alpha = 0.1f), Offset(0f, y * step), Offset(size.width, y * step), 1f)
                        }
                        
                        // Draw simulated spots
                        val spotW = 20.dp.toPx()
                        val spotH = 12.dp.toPx()
                        repeat(6) { r ->
                            repeat(8) { c ->
                                val x = 40.dp.toPx() + c * (spotW + 4.dp.toPx())
                                val y = 40.dp.toPx() + r * (spotH + 12.dp.toPx())
                                drawRoundRect(BorderColor, Offset(x, y), Size(spotW, spotH), CornerRadius(2.dp.toPx()))
                            }
                        }
                    }
                    
                    if (mySpot != null) {
                        // car marker
                        Text("🚗", modifier = Modifier.align(Alignment.Center).offset(x = 30.dp, y = (-20).dp), fontSize = 24.sp)
                        Text(mySpot.id, color = CyanAccent, modifier = Modifier.align(Alignment.Center).offset(x = 55.dp, y = (-45).dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        
                        // User marker (me)
                        Box(modifier = Modifier.align(Alignment.Center).offset(x = (-60).dp, y = 60.dp).size(16.dp).background(RedAccent, CircleShape).border(2.dp, Color.White, CircleShape))
                    } else {
                        Text("No vehicle parked", color = TextTertiary, modifier = Modifier.align(Alignment.Center))
                    }
                    
                    // Elevator marker
                    Surface(color = YellowAccent, shape = RoundedCornerShape(4.dp), modifier = Modifier.align(Alignment.TopStart).padding(12.dp)) {
                        Text(selectedElevator, color = Color.Black, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ElevatorBtn(name: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = if (isSelected) CyanAccent.copy(alpha = 0.12f) else TabColor,
        border = BorderStroke(1.dp, if (isSelected) CyanAccent else BorderColor),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(name, modifier = Modifier.padding(vertical = 12.dp), color = if (isSelected) CyanAccent else TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
fun ReportTab(onReported: () -> Unit) {
    var spotId by remember { mutableStateOf("") }
    var hasPicture by remember { mutableStateOf(false) }
    
    Surface(color = CardColor, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, BorderColor)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("REPORT MISUSE", color = RedAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(11.dp))
            OutlinedTextField(
                value = spotId,
                onValueChange = { if (it.length <= 3) spotId = it.filter { c -> c.isDigit() } },
                placeholder = { Text("Enter 3-digit spot ID", color = TextTertiary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = TabColor,
                    focusedContainerColor = TabColor,
                    unfocusedBorderColor = BorderColor,
                    focusedBorderColor = CyanAccent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(Modifier.height(12.dp))
            
            // Mock Camera Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TabColor)
                    .border(1.dp, if (hasPicture) GreenAccent else BorderColor, RoundedCornerShape(10.dp))
                    .clickable { hasPicture = true },
                contentAlignment = Alignment.Center
            ) {
                if (hasPicture) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenAccent, modifier = Modifier.size(48.dp))
                    Text("Picture captured", color = GreenAccent, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp), fontSize = 12.sp)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Tap to take verification picture", color = TextTertiary, fontSize = 12.sp)
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { 
                    onReported()
                    spotId = ""
                    hasPicture = false
                },
                enabled = spotId.length == 3 && hasPicture,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                shape = RoundedCornerShape(11.dp)
            ) {
                Text("Submit Report", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun WalletTab(user: User, onTopUp: (Double) -> Unit) {
    var showTopUpDialog by remember { mutableStateOf(false) }
    
    Surface(color = CardColor, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, BorderColor)) {
        Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("AVAILABLE BALANCE", color = TextSecondary, fontSize = 9.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("AED", color = TextSecondary, fontSize = 20.sp, modifier = Modifier.padding(bottom = 6.dp, end = 4.dp))
                Text(String.format(Locale.US, "%.2f", user.balance), color = CyanAccent, fontSize = 44.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = { showTopUpDialog = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                shape = RoundedCornerShape(11.dp)
            ) {
                Text("+ Top Up", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showTopUpDialog) {
        TopUpDialog(onDismiss = { showTopUpDialog = false }, onConfirm = { amt ->
            onTopUp(amt)
            showTopUpDialog = false
        })
    }
}

@Composable
fun TopUpDialog(onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("Apple Pay") }
    val methods = listOf("Apple Pay", "Samsung Pay", "Credit/Debit Card", "Nol Card", "Netbanking")

    Dialog(onDismissRequest = onDismiss) {
        Surface(color = CardColor, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, BorderColor)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Top Up Balance", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() } },
                    placeholder = { Text("Enter amount (AED)", color = TextTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )
                Spacer(Modifier.height(16.dp))
                Text("Select Payment Method", color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Column(modifier = Modifier.verticalScroll(rememberScrollState()).heightIn(max = 200.dp)) {
                    methods.forEach { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMethod = method }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedMethod == method, onClick = { selectedMethod = method })
                            Text(method, color = TextPrimary, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { amount.toDoubleOrNull()?.let { onConfirm(it) } },
                    enabled = amount.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Confirm Top Up", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ProfileTab(user: User, onUpdateUser: (User) -> Unit, onLogout: () -> Unit) {
    var isEditing by remember { mutableStateOf(false) }
    var firstName by remember { mutableStateOf(user.firstName) }
    var lastName by remember { mutableStateOf(user.lastName) }
    var phone by remember { mutableStateOf(user.phone) }
    
    var showAddCar by remember { mutableStateOf(false) }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Surface(color = CardColor, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, BorderColor)) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("MY PROFILE", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { 
                        if (isEditing) {
                            onUpdateUser(user.copy(firstName = firstName, lastName = lastName, phone = phone))
                        }
                        isEditing = !isEditing 
                    }) {
                        Text(if (isEditing) "SAVE" else "EDIT", color = CyanAccent)
                    }
                }
                
                if (isEditing) {
                    ProfileEditField("First Name", firstName) { firstName = it }
                    ProfileEditField("Last Name", lastName) { lastName = it }
                    ProfileEditField("Phone Number", phone) { phone = it }
                } else {
                    Text("${user.firstName} ${user.lastName}", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(user.phone, color = TextSecondary, fontSize = 14.sp)
                }
                
                Spacer(Modifier.height(20.dp))
                Text("MY VEHICLES", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                
                user.cars.forEach { car ->
                    TicketRow(car.plate, "${car.color} ${car.make} ${car.model}", TextPrimary)
                }
                
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { showAddCar = true },
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TabColor),
                    border = BorderStroke(1.dp, BorderColor),
                    shape = RoundedCornerShape(11.dp)
                ) {
                    Text("+ Add Car", color = TextPrimary, fontSize = 12.sp)
                }
                
                Spacer(Modifier.height(30.dp))
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                    shape = RoundedCornerShape(11.dp)
                ) {
                    Text("Sign Out", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(100.dp))
    }

    if (showAddCar) {
        AddCarDialog(onDismiss = { showAddCar = false }, onAdd = { newCar ->
            onUpdateUser(user.copy(cars = user.cars + newCar))
            showAddCar = false
        })
    }
}

@Composable
fun ProfileEditField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, color = TextSecondary, fontSize = 10.sp)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .drawBehind {
                    drawLine(BorderColor, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary)
        )
    }
}

@Composable
fun AddCarDialog(onDismiss: () -> Unit, onAdd: (Car) -> Unit) {
    var plate by remember { mutableStateOf("") }
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(color = CardColor, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, BorderColor)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Add New Vehicle", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = plate, onValueChange = { plate = it }, label = { Text("Plate Number") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = make, onValueChange = { make = it }, label = { Text("Make (e.g. Toyota)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Model (e.g. Camry)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = color, onValueChange = { color = it }, label = { Text("Color") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { onAdd(Car(plate, make, model, color)) },
                    enabled = plate.isNotEmpty() && make.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Add Vehicle", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun formatTime(s: Int): String {
    val h = (s / 3600).toString().padStart(2, '0')
    val m = ((s % 3600) / 60).toString().padStart(2, '0')
    val sc = (s % 60).toString().padStart(2, '0')
    return "$h:$m:$sc"
}
