package si.uni_lj.fri.pbd.pbd2025_lab_9

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.jetpacklab.SampleData
import si.uni_lj.fri.pbd.pbd2025_lab_9.ui.theme.Pbd2025lab9Theme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Pbd2025lab9Theme {
                MessageList(SampleData.messageListSample)
            }
        }
    }
}

data class Message(val author: String, val body: String, val imgR: String)

@Composable
fun MessageCard(msg: Message) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }

    val surfaceColor: Color by animateColorAsState(
        if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        label = "Background color animation"
    )

    Row(modifier = Modifier.padding(all = 8.dp)) {
        Image (
            painter = painterResource(context.resources.getIdentifier(msg.imgR, "drawable", context.packageName)),
            contentDescription = "Profile picture",
            modifier = Modifier.size(40.dp).clip(CircleShape),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 1.dp,
            color = surfaceColor,
            modifier = Modifier
                .animateContentSize()
                .padding(1.dp),
        ) {
            Column(modifier = Modifier.clickable { isExpanded = !isExpanded }) {
                Text(text = msg.author)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = msg.body,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    modifier = Modifier.padding(all = 4.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewMessageCard() {
    val msg = Message("Android", "Preview body", "pic1")
    Pbd2025lab9Theme {
        MessageCard(msg)
    }
}

@Composable
fun MessageList(messages: List<Message>) {
    LazyColumn {
        items(messages) { message ->
            MessageCard(message)
        }
    }
}

@Preview
@Composable
fun PreviewMessageLit() {
    MessageList(SampleData.messageListSample)
}