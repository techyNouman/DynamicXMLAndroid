package com.example.dynamicxmlandroid

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dynamicxmlandroid.databinding.ActivityMainBinding
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.collections.set

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val inputFieldMap = mutableMapOf<String, View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sampleXmlResponse = """
            <screen title="User Registration">
    <layout type="vertical">
        <!-- Name -->
        <component type="textField" id="name" label="Name" hint="Enter your name"/>

        <!-- Email -->
        <component type="textField" id="email" label="Email" hint="Enter your email"/>

        <!-- Country dropdown -->
        <component type="dropdown" id="country" label="Select country">
            <option value="us" label="USA"/>
            <option value="in" label="India"/>
            <option value="uk" label="UK"/>
            <option value="au" label="Australia"/>
        </component>

        <!-- Gender radio buttons -->
        <component type="radioGroup" id="gender" label="Select gender">
            <option value="male" label="Male"/>
            <option value="female" label="Female"/>
            <option value="other" label="Other"/>
        </component>

        <!-- Preferred contact method -->
        <component type="dropdown" id="contactMethod" label="Preferred Contact Method">
            <option value="email" label="Email"/>
            <option value="phone" label="Phone"/>
            <option value="none" label="Do not contact"/>
        </component>

        <!-- Newsletter switch -->
        <component type="switch" id="subscribe" label="Subscribe to newsletter" default="true"/>

        <!-- Submit Button -->
        <component type="button" id="submitBtn" text="Submit" action="submitForm"/>
        
        <layout type="horizontal">
            <component type="button" id="resetBtn" text="Reset" action="resetForm"/>
            <component type="button" id="cancelBtn" text="Cancel" action="cancelForm"/>
        </layout>
    </layout>
</screen>
        """.trimIndent()
        parseAndBuildUi(sampleXmlResponse)

    }

    private fun parseAndBuildUi(xmlString: String) {
        binding.dynamicContainer.removeAllViews() // Clear previous UI

        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(xmlString.reader())

            var eventType = parser.eventType
            var currentLayout: ViewGroup = binding.dynamicContainer // Start with the root container

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "screen" -> {
                                val title = parser.getAttributeValue(null, "title")
                                this.title = title // Set activity/toolbar title
                            }

                            "layout" -> {
                                val layoutType = parser.getAttributeValue(null, "type")
                                val newLayout = createLayoutContainer(layoutType)
                                currentLayout.addView(newLayout)
                                currentLayout = newLayout // New current container
                            }

                            "component" -> {
                                val componentType = parser.getAttributeValue(null, "type")
                                val view =
                                    createViewFromXmlComponent(parser, componentType, currentLayout)
                                view?.let { currentLayout.addView(it) }
                            }
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        if (parser.name == "layout") {
                            // Go back to the parent layout if currentLayout is not the root
                            if (currentLayout.parent is ViewGroup && currentLayout != binding.dynamicContainer) {
                                currentLayout = currentLayout.parent as ViewGroup
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle parsing error, show error message in UI
            val errorText = TextView(this)
            errorText.text = "Error parsing UI: ${e.message}"
            binding.dynamicContainer.addView(errorText)
        }
    }


    private fun createLayoutContainer(layoutType: String?): ViewGroup {
        val newLinearLayout = LinearLayout(this)
        when (layoutType) {
            "vertical" -> newLinearLayout.orientation = LinearLayout.VERTICAL
            "horizontal" -> newLinearLayout.orientation = LinearLayout.HORIZONTAL
            // Add more layout types like RelativeLayout, FrameLayout if needed
            else -> newLinearLayout.orientation = LinearLayout.VERTICAL // Default
        }
        newLinearLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        return newLinearLayout
    }

    private fun createViewFromXmlComponent(
        parser: XmlPullParser,
        componentType: String?,
        currentLayout: ViewGroup
    ): View? {
        val context = this
        return when (componentType) {
            "textField" -> {
                val id = parser.getAttributeValue(null, "id") ?: return null
                val hint = parser.getAttributeValue(null, "hint") ?: ""
                val label = parser.getAttributeValue(null, "label") ?: ""

                val container = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = 16 }
                }

                val labelView = TextView(context).apply {
                    text = label
                }

                val editText = EditText(context).apply {
                    this.hint = hint
                }

                container.addView(labelView)
                container.addView(editText)
                inputFieldMap[id] = editText

                container
            }

            "dropdown" -> {
                val id = parser.getAttributeValue(null, "id") ?: return null
                val label = parser.getAttributeValue(null, "label") ?: ""
                val items = mutableListOf<String>()

                // Collect options
                var eventType = parser.eventType
                while (!(eventType == XmlPullParser.END_TAG && parser.name == "component")) {
                    if (eventType == XmlPullParser.START_TAG && parser.name == "option") {
                        val labelAttr = parser.getAttributeValue(null, "label") ?: ""
                        items.add(labelAttr)
                    }
                    eventType = parser.next()
                }

                // Create label + spinner layout
                val container = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                }

                val textView = TextView(context).apply {
                    text = label
                }
                val spinner = Spinner(context).apply {
                    adapter =
                        ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, items)
                    inputFieldMap[id] = this
                }

                container.addView(textView)
                container.addView(spinner)
                container
            }

            "radioGroup" -> {
                val id = parser.getAttributeValue(null, "id") ?: return null
                val label = parser.getAttributeValue(null, "label") ?: ""

                val container = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                }

                val labelView = TextView(context).apply {
                    text = label
                }

                val radioGroup = RadioGroup(context).apply {
                    orientation = RadioGroup.VERTICAL
                }

                // Collect radio options
                var eventType = parser.eventType
                while (!(eventType == XmlPullParser.END_TAG && parser.name == "component")) {
                    if (eventType == XmlPullParser.START_TAG && parser.name == "option") {
                        val value = parser.getAttributeValue(null, "value") ?: ""
                        val labelText = parser.getAttributeValue(null, "label") ?: ""
                        val radioButton = RadioButton(context).apply {
                            text = labelText
                            tag = value // store value
                        }
                        radioGroup.addView(radioButton)
                    }
                    eventType = parser.next()
                }

                inputFieldMap[id] = radioGroup
                container.addView(labelView)
                container.addView(radioGroup)
                container
            }

            "button" -> {
                val id = parser.getAttributeValue(null, "id") ?: return null
                val text = parser.getAttributeValue(null, "text") ?: "Button"
                val action = parser.getAttributeValue(null, "action")

                val button = Button(context).apply {
                    this.text = text

                    // Check parent layout orientation
                    val isParentHorizontal =
                        (currentLayout as? LinearLayout)?.orientation == LinearLayout.HORIZONTAL
                    layoutParams = if (isParentHorizontal) {
                        LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                            .apply {
                                marginEnd = 8
                            }
                    } else {
                        LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            topMargin = 16
                        }
                    }

                    setOnClickListener {
                        when (action) {
                            "submitForm" -> handleFormSubmission()
                            "resetForm" -> resetFormFields()
                            "cancelForm" -> Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT)
                                .show()

                            else -> Toast.makeText(context, "Action: $action", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }

                    inputFieldMap[id] = this
                }

                button
            }


            "switch" -> {
                val id = parser.getAttributeValue(null, "id") ?: return null
                val label = parser.getAttributeValue(null, "label") ?: "Toggle"
                val defaultValue = parser.getAttributeValue(null, "default")?.toBoolean() ?: false

                Switch(context).apply {
                    text = label
                    isChecked = defaultValue
                    inputFieldMap[id] = this
                }
            }

            else -> null
        }
    }

    private fun handleFormSubmission() {
        val formData = JSONObject()
        var isValid = true

        for ((id, view) in inputFieldMap) {
            when (view) {
                is EditText -> {
                    val text = view.text.toString().trim()
                    if (text.isEmpty()) {
                        view.error = "Required"
                        isValid = false
                    } else {
                        formData.put(id, text)
                    }
                }

                is Spinner -> {
                    val selected = view.selectedItem.toString()
                    if (selected.isEmpty()) {
                        Toast.makeText(this, "Please select $id", Toast.LENGTH_SHORT)
                            .show()
                        isValid = false
                    } else {
                        formData.put(id, selected)
                    }
                }

                is RadioGroup -> {
                    val selectedId = view.checkedRadioButtonId
                    if (selectedId == -1) {
                        Toast.makeText(this, "Please select $id", Toast.LENGTH_SHORT)
                            .show()
                        isValid = false
                    } else {
                        val radioButton = view.findViewById<RadioButton>(selectedId)
                        val value = radioButton.tag?.toString() ?: radioButton.text.toString()
                        formData.put(id, value)
                    }
                }

                is Switch -> {
                    formData.put(id, view.isChecked)
                }

            }
        }

        if (isValid) {
            Log.e("DynamicForm", "Form JSON: $formData")
            Toast.makeText(this, "Form submitted!", Toast.LENGTH_SHORT).show()
            // TODO: send formData to API
        } else {
            Toast.makeText(this, "Please fix the errors", Toast.LENGTH_SHORT).show()
        }
    }


    private fun resetFormFields() {
        for ((_, view) in inputFieldMap) {
            when (view) {
                is EditText -> view.setText("")
                is Spinner -> view.setSelection(0)
                is RadioGroup -> view.clearCheck()
                is Switch -> view.isChecked = false
                is CheckBox -> view.isChecked = false
            }
        }
    }
}