#!/usr/bin/env bash
#
# Open Hospital (www.open-hospital.org)
# Copyright © 2006-2026 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
#
# Open Hospital is a free and open source software for healthcare data management.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# https://www.gnu.org/licenses/gpl-3.0-standalone.html
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program. If not, see <https://www.gnu.org/licenses/>.
#
# Checks that the launchers agree on the third-party software they download.
#
# Each launcher pins the JRE, MariaDB and Tomcat it fetches, so a release update has to touch
# every one of them. When one is missed the mismatch is invisible until somebody runs that
# platform: in 1.15.1 oh.sh and oh.ps1 moved to JRE 17.0.20.1 while ohmac.sh stayed on 17.0.16,
# and before that ohmac.sh sat on a Java 11 JRE for an entire release cycle.
#
# Run from anywhere: .github/scripts/check-launcher-versions.sh

set -uo pipefail

cd "$(dirname "$0")/../.." || exit 1

failures=0

err() {
	echo "FAIL: $*" >&2
}

# Reads VAR="value" from a shell launcher, or $script:VAR="value" from the PowerShell one, and
# writes it to stdout. Only an assignment at the start of a line counts, which leaves out the
# commented-out alternatives each launcher keeps around.
read_pin() {
	local file=$1 var=$2 prefix='' value
	[[ $file == *.ps1 ]] && prefix='\$script:'
	value=$(sed -nE "s/^${prefix}${var}=\"([^\"]*)\".*/\1/p" "$file")
	if [ -z "$value" ]; then
		err "$file: no $var= assignment found - has the variable been renamed?"
		return 1
	fi
	if [ "$(printf '%s\n' "$value" | wc -l)" -ne 1 ]; then
		err "$file: $var is assigned more than once, cannot tell which one wins"
		return 1
	fi
	printf '%s\n' "$value"
}

# The JRE is pinned as a full Azul archive name whose last segment is the platform:
# zulu17.68.203-ca-fx-jre17.0.20.1-linux_x64 vs -win_x64 vs -macosx_aarch64. Compare everything
# before that segment, so the same runtime on three platforms counts as a match.
jre_identity() {
	local distro=$1 identity
	identity=$(printf '%s\n' "$distro" | sed -nE 's/^(zulu[0-9.]+-ca(-fx)?-jre[0-9.]+)-.*/\1/p')
	if [ -z "$identity" ]; then
		err "cannot read a Zulu JRE version out of '$distro' - the naming scheme has changed, so this check needs updating too"
		return 1
	fi
	printf '%s\n' "$identity"
}

# Compares one pin across the launchers that carry it, listing every value when they disagree.
compare_pin() {
	local label=$1 var=$2 transform=$3
	shift 3
	local file value reference='' report='' mismatch=0

	for file in "$@"; do
		if ! value=$(read_pin "$file" "$var"); then
			failures=$((failures + 1))
			return
		fi
		if [ -n "$transform" ] && ! value=$("$transform" "$value"); then
			failures=$((failures + 1))
			return
		fi
		report+="    $file: $value"$'\n'
		if [ -z "$reference" ]; then
			reference=$value
		elif [ "$value" != "$reference" ]; then
			mismatch=1
		fi
	done

	if [ "$mismatch" -ne 0 ]; then
		err "the launchers pin different $label versions:"
		printf '%s' "$report" >&2
		failures=$((failures + 1))
	else
		echo "ok: $label $reference"
	fi
}

# oh.bat only forwards to oh.ps1 and pins nothing of its own, so it is not compared here.
compare_pin "JRE"     JAVA_DISTRO    jre_identity oh.sh oh.ps1 ohmac.sh
compare_pin "JRE URL" JAVA_URL       ''           oh.sh oh.ps1 ohmac.sh

# ohmac.sh carries neither: on macOS the database comes from Homebrew and the API is not served
# from a bundled Tomcat, so these two are only compared between the launchers that download them.
compare_pin "MariaDB" MYSQL_VERSION  ''           oh.sh oh.ps1
compare_pin "Tomcat"  TOMCAT_VERSION ''           oh.sh oh.ps1

# MYSQL32_VERSION is deliberately left out: oh.sh and oh.ps1 have pinned different 32-bit MariaDB
# builds since "Updates for 1.11.3" (#1145), so comparing it would report a difference that is
# meant to be there.

echo
if [ "$failures" -ne 0 ]; then
	echo "$failures launcher check(s) failed." >&2
	exit 1
fi
echo "All launchers agree."
