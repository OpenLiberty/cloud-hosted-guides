function addAccountReqText() {
    var newUserText = document.querySelector('#login-form')

    if (newUserText) {
        newUserText.insertAdjacentHTML('afterend', '<span class="text register-explain">To prevent abuse of our cloud hosted environments we require users to authenticate themselves, this can be done via the provided social login options or by creating a account.</span>');
        newUserText.style.maxWidth = "80%";
    }
}

function fixRegisterPage() {
    var ssoUsed = document.querySelector('.js-auth-warning')
    var registerForm = document.querySelector('#register')
    var loginForm = document.querySelector('#login')
    var signInLink = document.querySelector('.toggle-form')


    if (ssoUsed) {
        if (registerForm) {
            registerForm.style.display = "block";
        }
        if (loginForm) {
            loginForm.style.display = "block";
        }
        if (signInLink){
            signInLink.innerHTML = '';
        }
    }
}

function changePageText() {
    var signIn = document.querySelector('#login-form h2')
    var emailReminder = document.querySelector('#login-email-desc')
    var createAccount = document.querySelector('#login-form > div.toggle-form > a')
    var forgotPassword = document.querySelector('#login > div.form-field.password-password > button')

    if (emailReminder) {
        emailReminder.textContent = 'The email address you used to create an Open Liberty SkillsNetwork account.'
    }
    if (forgotPassword) {
        forgotPassword.textContent = 'Forgot your password?'
    }
}

function addFooter() {
    var bodyWindow = document.querySelector('body')

    bodyWindow.insertAdjacentHTML('afterend', '<footer> <div> <p> <a rel="noopener" target="_blank" href="https://ide.skillsnetwork.site/legal">Terms of Use</a> | <a rel="noopener" target="_blank" href="https://openliberty.skillsnetwork.site/privacy">Privacy Notice</a> </p> </div> <div class="footer__copyright"> © Copyright Open Liberty Skills Network 2021 </div> </footer>');
}


setTimeout(function() {
    //addAccountReqText();
    // fixRegisterPage();
    changePageText();
    // addFooter();
},100);
