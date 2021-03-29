header.header-global {
    background: #313653;
}
.header-global .nav-global li a, .header-global .nav-global div a {
    color: #eee;
}
header.header-global .search-group form.search .search-term {
    display: none;
}
header.header-global .search-group form.search .search-button {
    display: none;
}
/* hide header items */
.header-global .nav-global {
    display: none;
}
body {
    background: #fbfbfb;
}
.content-wrapper {
    background: #fbfbfb;
    margin-top: 80px;
}
.wrapper-footer {
    background: #d6d6d6;
}
.header-global .user .user-link .label-username {
    color: #eee;
}

.header-global .user .dropdown {
    background: none;
    color: #eee;
}

/* Quicklabs page */
.btn-quicklabs-action {
    color: #2c2e50;
    background: #abd155;
    box-shadow: none;
    border-radius: 4px;
}
.btn-quicklabs-action:hover {
    background: #abd155;
}

/* Hide quicklabs code */
.row.course-details-header h2 {
    display: none;
}

.row course-details-header {
    display: none;
}

/* Quicklabs header */
.row.course-details-header h1 {
    @import url('https://fonts.googleapis.com/css2?family=Asap:wght@400;500;600;700&display=swap');
    font-family: 'Asap', sans-serif;
    font-size: 36px;
    color: #2C2E50;
    font-weight: 550;
    letter-spacing: 0;
    margin:0;
    padding: 0;
}
.row.course-details-content p {
    @import url('https://fonts.googleapis.com/css2?family=Asap:wght@400;500;600;700&display=swap');
    font-family: 'Asap', sans-serif;
    font-size: 18px;
    color: #6B7797;
    letter-spacing: 0;
}

/* Optionally hide the entire side area */
.row.course-details-content .col-md-4 {
    display: none;
}

.effort {
    padding-top: 2em;
    display: flex;
    align-items:center;
}

#OLclock {
    padding-right: 0.5em;
}

.content {
    color: #FF1493;
}

.header-global .primary>a:hover, .header-global .primary>a:focus, .header-global .primary>a:active {
    background: inherit;
    border: .2em solid white;
    border-radius: .2em;
}

#content > div > section {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    align-items: stretch;
    padding-bottom: 2rem;
}

#content > div > section:after {
    content: "";
    flex-basis: 32%;
}