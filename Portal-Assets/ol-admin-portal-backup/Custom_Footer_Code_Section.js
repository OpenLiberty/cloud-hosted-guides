< script >
    (function() {
        // Move the 'Launch quicklab button under the content'
        if (window.location.pathname.startsWith("/quicklab")) {
            document.querySelector('.row.course-details-content .col-md-8')
                .append(document.querySelector('.row.course-details-header form, .row.course-details-header a'));
        }
    })();

(function() {
    var accessButton = document.querySelector('.btn-quicklabs-action')

    if (accessButton) {
        accessButton.innerText = "Access cloud-hosted guide";
    }
})();

(function() {
    document.querySelector('.user-link').href = "https://courses.openliberty.skillsnetwork.site/account/settings";
    document.querySelector(".dropdown-menu a[href='/profile']").innerText = "Profile"
    document.querySelector(".dropdown-menu a[href='/favorites']").innerText = "Favorites"
})(); <
/script>