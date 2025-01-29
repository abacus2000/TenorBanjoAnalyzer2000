const synth = new Tone.PolySynth(Tone.Synth).toDestination();

async function playChord(notes) {
    await Tone.start();
    const parsedNotes = notes.split(', ');
    const now = Tone.now();
    parsedNotes.forEach((note, index) => {
        synth.triggerAttackRelease(note, "4n", now + index * 0.5);
    });
    setTimeout(() => {
        synth.releaseAll();
    }, parsedNotes.length * 500 + 500);
}

let allChords = []; // list to store all chord data

document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM fully loaded');

    const fetchButton = document.getElementById('fetchButton');
    console.log('Fetch button:', fetchButton);
    if (fetchButton) {
        fetchButton.addEventListener('click', fetchData);
    }
    document.getElementById('patternSelector').addEventListener('change', filterChords);
    document.getElementById('maxVariationsSelector').addEventListener('change', filterChords);
    document.getElementById('ascendingNotesSelector').addEventListener('change', filterChords);
});

function fetchData() {
    const key = document.getElementById('keySelector').value;
    const scaleType = document.getElementById('scaleTypeSelector').value;

    const apiUrl = `https://l285rdgjx4.execute-api.us-east-1.amazonaws.com/dev/progression?key=${key}&scale_type=${scaleType}`;

    fetch(apiUrl)
        .then(response => response.json())
        .then(data => {
            console.log("API response:", data);
            if (data.error) {
                throw new Error(data.error);
            }
            if (!data.scale) {
                throw new Error("Scale data is missing");
            }

            allChords = data.chords; // store all chords data
            updateUI(data);
            filterChords(); // initial filtering
        })
        .catch(error => {
            console.error('Error:', error);
            document.getElementById('result').innerHTML = `An error occurred while fetching the data: ${error.message}`;
        });
}

function isAscending(notes) {
    const parsedNotes = notes.split(', ').map(note => {
        const [noteName, octave] = note.split(/(\d+)/);
        return { name: noteName, octave: parseInt(octave) };
    });

    let lowestNote = parsedNotes[0];
    for (let i = 1; i < parsedNotes.length; i++) {
        const currentNote = parsedNotes[i];
        // allow descent within one octave
        if (currentNote.octave < lowestNote.octave - 1) {
            return false;
        }
        // update lowest note if current note is lower
        if (currentNote.octave < lowestNote.octave || 
            (currentNote.octave === lowestNote.octave && 
             currentNote.name.charCodeAt(0) < lowestNote.name.charCodeAt(0))) {
            lowestNote = currentNote;
        }
    }
    return true;
}


function updateUI(data) {
    const scaleTableDiv = document.getElementById('scaleTable');
    const progressionInfoDiv = document.getElementById('progressionInfo');

    // build scale table
    let scaleTable = `
        <h2>Scale</h2>
        <table>
            <tr>${data.scale.map((_, index) => `<th>${index + 1}</th>`).join('')}</tr>
            <tr>${data.scale.map(note => `<td>${note}</td>`).join('')}</tr>
        </table>`;
    scaleTableDiv.innerHTML = scaleTable;

    // build progression info table
    let progressionInfo = `
        <h2>Progression Information</h2>
        <table>
            <tr><th>Key</th><td>${data.key}</td></tr>
            <tr><th>Scale Type</th><td>${data.scale_type}</td></tr>
        </table>`;
    progressionInfoDiv.innerHTML = progressionInfo;
}

function filterChords() {
    const pattern = document.getElementById('patternSelector').value;
    const patternDegrees = pattern === 'All' ? null : pattern.split('-');
    const maxVariations = document.getElementById('maxVariationsSelector').value;
    const ascendingNotesOnly = document.getElementById('ascendingNotesSelector').value === 'true';
    const chordsTableDiv = document.getElementById('chordsTable');

    // create an object to store chords by the roman numeral (in the nashville id column)
    let chordsByNashville = {};

    allChords.forEach(chord => {
        chord.variations.forEach(variation => {
            if ((!patternDegrees || patternDegrees.includes(variation.ChordInfo['Nashville Number'])) &&
                (!ascendingNotesOnly || isAscending(variation.ChordInfo.Notes))) {
                if (!chordsByNashville[variation.ChordInfo['Nashville Number']]) {
                    chordsByNashville[variation.ChordInfo['Nashville Number']] = [];
                }
                // create a unique key for each chord variation
                const chordKey = `${chord.root}_${variation.ChordId}_${variation.Quality}_${variation.ChordInfo.Position}_${variation.ChordInfo.Frets}_${variation.ChordInfo.Notes}_${variation.ChordInfo.Inversion}_${variation.ChordInfo["Bass Note"]}`;
                
                // check if this chord variation already exists 
                if (!chordsByNashville[variation.ChordInfo['Nashville Number']].some(c => c.key === chordKey)) {
                    chordsByNashville[variation.ChordInfo['Nashville Number']].push({...chord, variation, key: chordKey});
                }
            }
        });
    });

    // sort chords by inversion number within each Nashville Number group
    Object.keys(chordsByNashville).forEach(nashville => {
        chordsByNashville[nashville].sort((a, b) => 
            a.variation.ChordInfo.Inversion - b.variation.ChordInfo.Inversion
        );
        // limit the number of variations based on maxVariations var
        if (maxVariations !== 'All') {
            chordsByNashville[nashville] = chordsByNashville[nashville].slice(0, parseInt(maxVariations));
        }
    });

    // builds chords table with filtered and ordered chords
    let chordsTable = `
        <h2>Chord Variations</h2>
        <table>
            <tr>
                <th>Root</th>
                <th>Chord ID</th>
                <th>Quality</th>
                <th>Position</th>
                <th>Frets</th>
                <th>Notes</th>
                <th>Inv No.</th>
                <th>Bass</th>
                <th>Nashville Number</th>
                <th>Play</th>
            </tr>`;

    const nashvilleNumbers = patternDegrees || Object.keys(chordsByNashville).sort();
    nashvilleNumbers.forEach(degree => {
        if (chordsByNashville[degree]) {
            chordsByNashville[degree].forEach(({root, variation}) => {
                chordsTable += `
                    <tr>
                        <td>${root}</td>
                        <td>${variation.ChordId}</td>
                        <td>${variation.Quality}</td>
                        <td>${variation.ChordInfo.Position}</td>
                        <td>${variation.ChordInfo.Frets}</td>
                        <td>${variation.ChordInfo.Notes}</td>
                        <td>${variation.ChordInfo.Inversion}</td>
                        <td>${variation.ChordInfo["Bass Note"]}</td>
                        <td>${variation.ChordInfo["Nashville Number"]}</td>
                        <td><button class="play-button" onclick="playChord('${variation.ChordInfo.Notes}')">Play</button></td>
                    </tr>`;
            });
        }
    });

    chordsTable += '</table>';
    chordsTableDiv.innerHTML = chordsTable;
}
