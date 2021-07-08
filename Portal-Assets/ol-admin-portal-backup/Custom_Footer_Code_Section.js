<!-- Google Tag Manager (noscript) -->
<noscript><iframe src="https://www.googletagmanager.com/ns.html?id=GTM-KTRJ86B"
height="0" width="0" style="display:none;visibility:hidden"></iframe></noscript>
<!-- End Google Tag Manager (noscript) -->


<script>
    (function() {
        // Move the 'Launch quicklab button under the content'
        if(window.location.pathname.startsWith("/quicklab")) {
            document.querySelector('.row.course-details-content .col-md-8')
                .append(document.querySelector('.row.course-details-header form, .row.course-details-header a'));
        }
    })();

    (function() {
        var accessButton = document.querySelector('.btn-quicklabs-action')
        
        if (accessButton) {
            accessButton.innerText="Access cloud-hosted guide";
        }
    })();
    
    (function() {
        var userLink = document.querySelector('.user-link');
        
        if (userLink) {
            userLink.href="https://courses.openliberty.skillsnetwork.site/account/settings";
            document.querySelector(".dropdown-menu a[href='/profile']").innerText="Profile"
            document.querySelector(".dropdown-menu a[href='/favorites']").innerText="Favorites"
        }
    })();
</script>

<script>
    // Rating page scripts
    const queryString = window.location.search;
    const urlParams = new URLSearchParams(queryString);
    var guideName = (urlParams.get('guide-name') ? urlParams.get('guide-name') : "")
    var guideID = (urlParams.get('guide-id') ? urlParams.get('guide-id') : "")
    
    if (guideName) {
        guideName = guideName.replace("/", "")
        guideName = guideName.replace("\\", "")
    }
    
    document.querySelector('#guide-name').outerHTML = "<h3 id=\"guide-name\">What did you think of the <em>" + guideName+ "</em> guide?</h3>"
    document.querySelector('#content > div > div > p:nth-child(5) > a').href += guideID + '/issues'
    document.querySelector('#content > div > div > p:nth-child(6) > a').href += guideID + '/pulls'
    
    var popup = document.getElementById("feedbackPopup");
    var closeButton = document.getElementById("close_button");
    
    document.querySelectorAll("#feedback_ratings img").forEach(face => 
        face.onclick = () => {
            if (guideName) {
                popup.style.display = "block";
                face.style.opacity = 1;
            }
        }
    )
    
    closeButton.onclick = function() {
        popup.style.display = "none";
    }
    
    window.onclick = function(event) {
      if (event.target == popup) {
        popup.style.display = "none";
      }
    }
</script>