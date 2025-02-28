/**
 * mobile-menu.js
 * Steuerung für das mobile Menü und angepasste Oberfläche
 */

document.addEventListener('DOMContentLoaded', () => {
    // Menü-Toggle
    const menuToggle = document.getElementById('menuToggle');
    const sideMenu = document.getElementById('sideMenu');
    const menuHint = document.getElementById('menuHint');
    
    // Tabs im Menü
    setupMenuTabs();
    
    // Menü-Öffnen/Schließen
    if (menuToggle && sideMenu) {
        menuToggle.addEventListener('click', () => {
            sideMenu.classList.toggle('active');
            menuToggle.classList.toggle('active');
            
            // Menü-Hilfe ausblenden, wenn Menü geöffnet wird
            if (menuHint && menuHint.classList.contains('visible')) {
                menuHint.classList.remove('visible');
                localStorage.setItem('menuHintShown', 'true');
            }
        });
        
        // Menü schließen, wenn auf das Spielfeld geklickt wird
        document.querySelector('.game-board').addEventListener('click', (e) => {
            // Nur schließen, wenn Menü offen ist und der Klick nicht im Menü erfolgt
            if (sideMenu.classList.contains('active')) {
                sideMenu.classList.remove('active');
                menuToggle.classList.remove('active');
            }
        });
        
        // Verhindern, dass Klicks im Menü das Menü schließen
        sideMenu.addEventListener('click', (e) => {
            e.stopPropagation();
        });
    }
    
    // Menü-Hint beim ersten Besuch anzeigen
    if (menuHint && !localStorage.getItem('menuHintShown')) {
        // Kurz verzögern, um Aufmerksamkeit zu erregen
        setTimeout(() => {
            menuHint.classList.add('visible');
            
            // Pulse-Effekt für den Menübutton
            if (menuToggle) {
                menuToggle.classList.add('menu-pulse');
            }
            
            // Hint nach einiger Zeit automatisch ausblenden
            setTimeout(() => {
                menuHint.classList.remove('visible');
                localStorage.setItem('menuHintShown', 'true');
                
                if (menuToggle) {
                    setTimeout(() => {
                        menuToggle.classList.remove('menu-pulse');
                    }, 2000);
                }
            }, 5000);
        }, 2000);
    }
    
    // Synchronisiere Scoreanzeige und andere Statuswerte
    syncUIValues();
});

/**
 * Richtet die Tab-Steuerung im Menü ein
 */
function setupMenuTabs() {
    const tabs = document.querySelectorAll('.control-tab');
    const tabContents = document.querySelectorAll('.tab-content');
    
    // Wenn keine Tabs vorhanden sind, beende die Funktion
    if (tabs.length === 0) return;
    
    tabs.forEach(tab => {
        tab.addEventListener('click', (e) => {
            e.stopPropagation(); // Verhindert, dass der Klick das Menü schließt
            
            // Aktiven Tab setzen
            tabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            
            // Aktiven Tab-Inhalt anzeigen
            const targetId = tab.getAttribute('data-target');
            tabContents.forEach(content => {
                content.classList.remove('active');
                if (content.id === targetId) {
                    content.classList.add('active');
                }
            });
        });
    });
    
    // Initial den ersten Tab aktivieren
    if (tabs.length > 0 && tabContents.length > 0) {
        tabs[0].classList.add('active');
        tabContents[0].classList.add('active');
    }
}

/**
 * Synchronisiert Werte zwischen den UI-Elementen
 */
function syncUIValues() {
    // Score synchronisieren mit dem Game-State
    const scoreElement = document.getElementById('scoreValue');
    if (scoreElement) {
        // Überwache Punktzahl-Änderungen
        const observer = new MutationObserver((mutations) => {
            // Aktualisiere andere DOM-Elemente mit derselben ID (falls vorhanden)
            const otherScoreElements = document.querySelectorAll('#scoreValue');
            const newValue = mutations[0].target.textContent;
            
            otherScoreElements.forEach(element => {
                if (element !== mutations[0].target) {
                    element.textContent = newValue;
                }
            });
        });
        
        observer.observe(scoreElement, { childList: true });
    }
    
    // Verbindungsstatus synchronisieren
    const connectionStatus = document.getElementById('connectionStatus');
    if (connectionStatus) {
        const observer = new MutationObserver((mutations) => {
            const otherStatusElements = document.querySelectorAll('#connectionStatus');
            const newText = mutations[0].target.textContent;
            const newClass = mutations[0].target.className;
            
            otherStatusElements.forEach(element => {
                if (element !== mutations[0].target) {
                    element.textContent = newText;
                    element.className = newClass;
                }
            });
        });
        
        observer.observe(connectionStatus, { childList: true, attributes: true });
    }
    
    // Bei Fenstergröße-Änderung Canvas anpassen
    window.addEventListener('resize', () => {
        const canvas = document.getElementById('gameCanvas');
        if (canvas) {
            canvas.width = window.innerWidth;
            canvas.height = window.innerHeight;
        }
    });
}
