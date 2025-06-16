Render a raw xml from API response into an android activity. 

XML:
  
    <screen title="User Registration">
  
    <layout type="vertical">
       
      <component type="textField" id="name" label="Name" hint="Enter your name"/>

        <component type="textField" id="email" label="Email" hint="Enter your email"/>

        <component type="dropdown" id="country" label="Select country">
            <option value="us" label="USA"/>
            <option value="in" label="India"/>
            <option value="uk" label="UK"/>
            <option value="au" label="Australia"/>
        </component>
        
        <component type="radioGroup" id="gender" label="Select gender">
            <option value="male" label="Male"/>
            <option value="female" label="Female"/>
            <option value="other" label="Other"/>
        </component>

        <component type="dropdown" id="contactMethod" label="Preferred Contact Method">
            <option value="email" label="Email"/>
            <option value="phone" label="Phone"/>
            <option value="none" label="Do not contact"/>
        </component>

        <component type="switch" id="subscribe" label="Subscribe to newsletter" default="true"/>

        <component type="button" id="submitBtn" text="Submit" action="submitForm"/>
        
        <layout type="horizontal">
            <component type="button" id="resetBtn" text="Reset" action="resetForm"/>
            <component type="button" id="cancelBtn" text="Cancel" action="cancelForm"/>
        </layout>
    </layout>
    </screen>


  ## 📸 Screenshot

![App Screenshot](assets/screenshot.png)
